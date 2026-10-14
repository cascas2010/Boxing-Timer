package com.walterchan.boxingtimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public final class TimerService extends Service {
    private static final String TAG = "BoxingTimerService";
    static final String ACTION_START = "com.walterchan.boxingtimer.START";
    static final String ACTION_TOGGLE = "com.walterchan.boxingtimer.TOGGLE";
    static final String ACTION_RESTART = "com.walterchan.boxingtimer.RESTART";
    static final String ACTION_END = "com.walterchan.boxingtimer.END";
    static final String ACTION_STATE = "com.walterchan.boxingtimer.STATE";
    static final String EXTRA_TRAINING = "training";
    static final String EXTRA_REST = "rest";
    static final String EXTRA_REMINDER = "reminder";
    static final String EXTRA_ROUNDS = "rounds";
    private static final String CHANNEL_ID = "active_training_timer";
    private static final int NOTIFICATION_ID = 41;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TimerState state;
    private SoundPool soundPool;
    private int stageBellId;
    private int countdownId;
    private boolean stageBellLoaded;
    private boolean countdownLoaded;
    private boolean pendingStageBell;
    private long stageBellGroupUntilMs;
    private long bellSession;
    private final CuePolicy cuePolicy = new CuePolicy();
    private int lastNotificationSecond = -1;

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            tick();
            if (state != null && state.active) handler.postDelayed(this, 100L);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        try {
            createChannel();
        } catch (RuntimeException error) {
            Log.e(TAG, "Unable to create foreground-service notification channel", error);
        }
        try {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setMaxStreams(8).setAudioAttributes(attrs).build();
        } catch (RuntimeException error) {
            Log.e(TAG, "Audio initialization failed; timer will continue silently", error);
            soundPool = null;
        }
        if (soundPool == null) {
            state = TimerState.load(this);
            return;
        }
        try {
            soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
                if (sampleId == stageBellId) stageBellLoaded = status == 0;
                if (sampleId == countdownId) countdownLoaded = status == 0;
                if (stageBellLoaded && pendingStageBell) {
                    pendingStageBell = false;
                    playBell(true);
                }
            });
        } catch (RuntimeException error) {
            Log.e(TAG, "Audio listener initialization failed; timer will continue silently", error);
            soundPool.release();
            soundPool = null;
            state = TimerState.load(this);
            return;
        }
        try {
            stageBellId = soundPool.load(this, R.raw.opening_bell_421471_loud, 1);
        } catch (RuntimeException error) {
            Log.e(TAG, "Opening bell load failed; stage cues disabled", error);
            stageBellId = 0;
        }
        try {
            countdownId = soundPool.load(this, R.raw.boxing_bell_v2, 1);
        } catch (RuntimeException error) {
            Log.e(TAG, "Countdown cue load failed; countdown cues disabled", error);
            countdownId = 0;
        }
        state = TimerState.load(this);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        long now = TimerState.now();
        if (ACTION_START.equals(action)) {
            bellSession++;
            int training = intent.getIntExtra(EXTRA_TRAINING, 180);
            int rest = intent.getIntExtra(EXTRA_REST, 60);
            int reminder = intent.getIntExtra(EXTRA_REMINDER, 10);
            int rounds = intent.getIntExtra(EXTRA_ROUNDS, 0);
            state.begin(training, rest, reminder, rounds, now);
            state.save(this);
            cuePolicy.onStageBell(state.phase, state.remainingSeconds(now));
            playBell(true);
        } else if (ACTION_TOGGLE.equals(action)) {
            state.toggle(now);
            state.save(this);
        } else if (ACTION_RESTART.equals(action)) {
            bellSession++;
            state.begin(state.trainingSeconds, state.restSeconds, state.reminderSeconds, state.roundLimit, now);
            state.save(this);
            cuePolicy.onStageBell(state.phase, state.remainingSeconds(now));
            playBell(true);
        } else if (ACTION_END.equals(action)) {
            bellSession++;
            handler.removeCallbacksAndMessages(null);
            state.end();
            state.save(this);
            broadcastState();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!state.active) {
            stopSelf();
            return START_NOT_STICKY;
        }
        try {
            startForeground(NOTIFICATION_ID, buildNotification());
        } catch (RuntimeException error) {
            Log.e(TAG, "Unable to enter foreground; stopping active timer safely", error);
            state.end();
            state.save(this);
            broadcastState();
            stopSelf();
            return START_NOT_STICKY;
        }
        handler.removeCallbacks(ticker);
        handler.post(ticker);
        broadcastState();
        return START_STICKY;
    }

    private void tick() {
        if (state == null || !state.active) return;
        long now = TimerState.now();
        int transitions = state.advance(now);
        int seconds = state.remainingSeconds(now);
        if (transitions > 0) {
            state.save(this);
            cuePolicy.onStageBell(state.phase, seconds);
            playBell(true);
        }
        if (!state.active) {
            broadcastState();
            stopForeground(STOP_FOREGROUND_REMOVE);
            handler.postDelayed(this::stopSelf, StageBellPattern.SOUND_DURATION_MS + 100L);
            return;
        }
        if (now >= stageBellGroupUntilMs
                && cuePolicy.shouldPlayCountdown(state.running, state.phase, seconds, state.reminderSeconds)) {
            playBell(false);
        }
        if (seconds != lastNotificationSecond || transitions > 0) {
            lastNotificationSecond = seconds;
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(NOTIFICATION_ID, buildNotification());
            broadcastState();
        }
    }

    private void playBell(boolean stageChange) {
        if (soundPool == null) return;
        if (stageChange && !stageBellLoaded) {
            if (stageChange) pendingStageBell = true;
            return;
        }
        if (!stageChange && !countdownLoaded) return;
        if (stageChange) {
            long now = TimerState.now();
            long scheduledSession = bellSession;
            stageBellGroupUntilMs = now + StageBellPattern.groupDurationMs();
            for (long delay : StageBellPattern.strikeDelaysMs()) {
                handler.postDelayed(() -> {
                    if (soundPool != null && state != null
                            && (state.active || state.completedNaturally) && bellSession == scheduledSession) {
                        safePlay(stageBellId);
                    }
                }, delay);
            }
        } else {
            safePlay(countdownId);
        }
    }

    private void safePlay(int soundId) {
        try {
            if (soundPool != null && soundId != 0) soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
        } catch (RuntimeException error) {
            Log.e(TAG, "Audio playback failed; timer continues silently", error);
        }
    }

    private Notification buildNotification() {
        String phase = getString(TimerState.PHASE_TRAINING.equals(state.phase) ? R.string.phase_training : R.string.phase_rest);
        String text = phase + "  " + TimerState.format(state.remainingSeconds(TimerState.now()))
                + "  ·  " + getString(R.string.round_status, state.currentRound,
                state.roundLimit == 0 ? "∞" : String.valueOf(state.roundLimit))
                + (state.running ? "" : getString(R.string.paused_suffix));
        Intent launch = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, launch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(text)
                .setContentIntent(pending)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setSound(null)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
    }

    private void broadcastState() {
        Intent update = new Intent(ACTION_STATE).setPackage(getPackageName());
        update.putExtra("active", state.active);
        update.putExtra("running", state.running);
        update.putExtra("phase", state.phase);
        update.putExtra("seconds", state.remainingSeconds(TimerState.now()));
        update.putExtra("completed", state.completedNaturally);
        update.putExtra("round", state.currentRound);
        update.putExtra("round_limit", state.roundLimit);
        sendBroadcast(update);
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.notification_channel_description));
        channel.setSound(null, null);
        channel.enableVibration(false);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (soundPool != null) soundPool.release();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
