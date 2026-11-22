package app.kodex.extension;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class VlcControls {
    private static final String TAG = "Kodex/VlcControls";

    // -------------------------------------------------------------
    // 1. Android Native Picture-in-Picture (PiP) Actions
    // -------------------------------------------------------------

    @TargetApi(Build.VERSION_CODES.O)
    public static List<RemoteAction> getPipActions(Context context) {
        List<RemoteAction> actions = new ArrayList<>();
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return actions;
        }

        try {
            String pkg = context.getPackageName();
            if (pkg == null) pkg = "org.videolan.vlc";

            Class<?> serviceClass = Class.forName("org.videolan.vlc.PlaybackService");

            // Customizable jump delay from Settings (defaults to 10s if not set)
            long jumpDelay = getVideoJumpDelay();

            int rewindIconId = getDrawableId(context, "ic_widget_rewind_10",
                    getDrawableId(context, "ic_player_rewind_10", android.R.drawable.ic_media_previous));
            int forwardIconId = getDrawableId(context, "ic_widget_forward_10",
                    getDrawableId(context, "ic_player_forward_10", android.R.drawable.ic_media_next));
            int playPauseIconId = getDrawableId(context, "ic_popup_pause",
                    getDrawableId(context, "ic_notif_pause", android.R.drawable.ic_media_pause));
            int nextIconId = getDrawableId(context, "ic_notif_next",
                    getDrawableId(context, "ic_player_next", android.R.drawable.ic_media_next));

            String seekDelayKey = pkg + ".EXTRA_CUSTOM_ACTION_ID";

            // 1. Rewind Action
            Intent rewindIntent = new Intent(pkg + ".remote.SeekBackward", null, context, serviceClass);
            rewindIntent.putExtra(seekDelayKey, jumpDelay);
            rewindIntent.putExtra("EXTRA_CUSTOM_ACTION_ID", jumpDelay);
            PendingIntent piRewind = PendingIntent.getService(context, 101, rewindIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            actions.add(new RemoteAction(
                    Icon.createWithResource(context, rewindIconId),
                    "Rewind",
                    "Rewind " + jumpDelay + "s",
                    piRewind
            ));

            // 2. Play/Pause Action
            Intent playPauseIntent = new Intent(pkg + ".remote.PlayPause", null, context, serviceClass);
            PendingIntent piPlayPause = PendingIntent.getService(context, 102, playPauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            actions.add(new RemoteAction(
                    Icon.createWithResource(context, playPauseIconId),
                    "Play/Pause",
                    "Play/Pause",
                    piPlayPause
            ));

            // 3. Fast-Forward Action
            Intent forwardIntent = new Intent(pkg + ".remote.SeekForward", null, context, serviceClass);
            forwardIntent.putExtra(seekDelayKey, jumpDelay);
            forwardIntent.putExtra("EXTRA_CUSTOM_ACTION_ID", jumpDelay);
            PendingIntent piForward = PendingIntent.getService(context, 103, forwardIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            actions.add(new RemoteAction(
                    Icon.createWithResource(context, forwardIconId),
                    "Forward",
                    "Forward " + jumpDelay + "s",
                    piForward
            ));

            // 4. Next Video Action
            Intent nextIntent = new Intent(pkg + ".remote.Forward", null, context, serviceClass);
            PendingIntent piNext = PendingIntent.getService(context, 104, nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            actions.add(new RemoteAction(
                    Icon.createWithResource(context, nextIconId),
                    "Next",
                    "Next Track",
                    piNext
            ));

        } catch (Throwable t) {
            Log.e(TAG, "Failed to build PiP actions", t);
        }

        return actions;
    }

    // -------------------------------------------------------------
    // 2. Floating Pop-up Player: Netflix-Style Double-Tap to Seek
    // -------------------------------------------------------------

    public static void onPopupDoubleTap(Object popupManagerObj, MotionEvent event) {
        if (popupManagerObj == null || event == null) return;
        try {
            Field serviceField = popupManagerObj.getClass().getDeclaredField("service");
            serviceField.setAccessible(true);
            Object serviceObj = serviceField.get(popupManagerObj);

            Field rootViewField = popupManagerObj.getClass().getDeclaredField("rootView");
            rootViewField.setAccessible(true);
            View rootView = (View) rootViewField.get(popupManagerObj);

            int width = (rootView != null && rootView.getWidth() > 0) ? rootView.getWidth() : 1000;
            float x = event.getX();

            long jumpDelay = getVideoDoubleTapJumpDelay();
            long deltaMs = jumpDelay * 1000L;
            int direction = (x < width / 2.0f) ? -1 : 1;

            seekService(serviceObj, deltaMs * direction);
        } catch (Throwable t) {
            Log.e(TAG, "Failed onPopupDoubleTap", t);
        }
    }

    // -------------------------------------------------------------
    // 3. Floating Pop-up Player: Initialization & Dedicated Buttons
    // -------------------------------------------------------------

    public static void initPopup(View rootView, Object vlcVout, Object popupManagerObj) {
        if (rootView != null && vlcVout != null) {
            try {
                for (Method m : rootView.getClass().getMethods()) {
                    if ("setVLCVOut".equals(m.getName())) {
                        m.invoke(rootView, vlcVout);
                        break;
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "Failed to call setVLCVOut", t);
            }
        }
        setupPopupControls(rootView, popupManagerObj);
    }

    private static void setupPopupControls(final View rootView, final Object popupManagerObj) {
        if (!(rootView instanceof ViewGroup) || popupManagerObj == null) return;
        final ViewGroup container = (ViewGroup) rootView;
        final Context context = rootView.getContext();

        container.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (container.findViewWithTag("kodex_popup_controls") != null) {
                        return;
                    }

                    int playPauseId = getIdentifier(context, "video_play_pause", "id");
                    final View playPauseView = playPauseId != 0 ? container.findViewById(playPauseId) : null;
                    if (playPauseView == null) return;

                    Field serviceField = popupManagerObj.getClass().getDeclaredField("service");
                    serviceField.setAccessible(true);
                    final Object serviceObj = serviceField.get(popupManagerObj);

                    final LinearLayout controlsRow = new LinearLayout(context);
                    controlsRow.setTag("kodex_popup_controls");
                    controlsRow.setOrientation(LinearLayout.HORIZONTAL);
                    controlsRow.setGravity(Gravity.CENTER);
                    controlsRow.setVisibility(playPauseView.getVisibility());

                    int iconSize = dpToPx(context, 34);
                    int iconPadding = dpToPx(context, 4);
                    int gap = dpToPx(context, 8);

                    // Prev Button
                    ImageView prevBtn = new ImageView(context);
                    prevBtn.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
                    prevBtn.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
                    prevBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    prevBtn.setImageResource(getDrawableId(context, "ic_notif_previous", android.R.drawable.ic_media_previous));
                    prevBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendPlaybackAction(context, serviceObj, "Backward");
                        }
                    });

                    // Rewind Button
                    ImageView rewindBtn = new ImageView(context);
                    LinearLayout.LayoutParams rewindLp = new LinearLayout.LayoutParams(iconSize, iconSize);
                    rewindLp.leftMargin = gap;
                    rewindBtn.setLayoutParams(rewindLp);
                    rewindBtn.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
                    rewindBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    rewindBtn.setImageResource(getDrawableId(context, "ic_widget_rewind_10",
                            getDrawableId(context, "ic_player_rewind_10", android.R.drawable.ic_media_previous)));
                    rewindBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            seekService(serviceObj, -getVideoJumpDelay() * 1000L);
                        }
                    });

                    // Center Spacer (over play/pause button)
                    View centerSpacer = new View(context);
                    int spacerWidth = playPauseView.getWidth() > 0 ? playPauseView.getWidth() : dpToPx(context, 48);
                    LinearLayout.LayoutParams spacerLp = new LinearLayout.LayoutParams(spacerWidth + gap * 2, 1);
                    centerSpacer.setLayoutParams(spacerLp);
                    centerSpacer.setVisibility(View.INVISIBLE);

                    // Forward Button
                    ImageView forwardBtn = new ImageView(context);
                    forwardBtn.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
                    forwardBtn.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
                    forwardBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    forwardBtn.setImageResource(getDrawableId(context, "ic_widget_forward_10",
                            getDrawableId(context, "ic_player_forward_10", android.R.drawable.ic_media_next)));
                    forwardBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            seekService(serviceObj, getVideoJumpDelay() * 1000L);
                        }
                    });

                    // Next Button
                    ImageView nextBtn = new ImageView(context);
                    LinearLayout.LayoutParams nextLp = new LinearLayout.LayoutParams(iconSize, iconSize);
                    nextLp.leftMargin = gap;
                    nextBtn.setLayoutParams(nextLp);
                    nextBtn.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
                    nextBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    nextBtn.setImageResource(getDrawableId(context, "ic_notif_next", android.R.drawable.ic_media_next));
                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendPlaybackAction(context, serviceObj, "Forward");
                        }
                    });

                    controlsRow.addView(prevBtn);
                    controlsRow.addView(rewindBtn);
                    controlsRow.addView(centerSpacer);
                    controlsRow.addView(forwardBtn);
                    controlsRow.addView(nextBtn);

                    // Attach to constraint layout
                    try {
                        Class<?> lpClass = Class.forName("androidx.constraintlayout.widget.ConstraintLayout$LayoutParams");
                        Constructor<?> ctor = lpClass.getConstructor(int.class, int.class);
                        Object rowLp = ctor.newInstance(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        Field topToTop = lpClass.getField("topToTop");
                        topToTop.set(rowLp, playPauseId);
                        Field bottomToBottom = lpClass.getField("bottomToBottom");
                        bottomToBottom.set(rowLp, playPauseId);
                        Field startToStart = lpClass.getField("startToStart");
                        startToStart.set(rowLp, 0); // 0 is PARENT_ID
                        Field endToEnd = lpClass.getField("endToEnd");
                        endToEnd.set(rowLp, 0);

                        container.addView(controlsRow, (ViewGroup.LayoutParams) rowLp);
                    } catch (Throwable t) {
                        container.addView(controlsRow);
                    }

                    // Keep controls visibility synchronized with playPauseView
                    playPauseView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            int vis = playPauseView.getVisibility();
                            if (controlsRow.getVisibility() != vis) {
                                controlsRow.setVisibility(vis);
                            }
                            return true;
                        }
                    });

                } catch (Throwable t) {
                    Log.e(TAG, "Error setting up popup controls", t);
                }
            }
        });
    }

    // -------------------------------------------------------------
    // 4. Helper Utilities
    // -------------------------------------------------------------

    private static void seekService(Object serviceObj, long deltaMs) {
        if (serviceObj == null) return;
        try {
            Method isSeekableMethod = serviceObj.getClass().getMethod("isSeekable");
            if (!((Boolean) isSeekableMethod.invoke(serviceObj))) return;

            Method getTimeMethod = serviceObj.getClass().getMethod("getTime");
            long currentTime = (Long) getTimeMethod.invoke(serviceObj);

            Method getLengthMethod = serviceObj.getClass().getMethod("getLength");
            long length = (Long) getLengthMethod.invoke(serviceObj);

            long targetTime = Math.max(0L, Math.min(length, currentTime + deltaMs));

            Method seekMethod = serviceObj.getClass().getMethod("seek", long.class);
            seekMethod.invoke(serviceObj, targetTime);
        } catch (Throwable t) {
            Log.e(TAG, "Error seeking service", t);
        }
    }

    private static void sendPlaybackAction(Context context, Object serviceObj, String actionSuffix) {
        boolean sent = false;
        try {
            String pkg = context.getPackageName();
            if (pkg == null) pkg = "org.videolan.vlc";
            String action = pkg + ".remote." + actionSuffix;
            Intent intent = new Intent(action);
            intent.setComponent(new ComponentName(context, "org.videolan.vlc.PlaybackService"));
            context.startService(intent);
            sent = true;
        } catch (Throwable ignored) {}

        if (!sent && serviceObj != null) {
            try {
                if ("Forward".equals(actionSuffix)) {
                    try {
                        Method m = serviceObj.getClass().getMethod("next", boolean.class);
                        m.invoke(serviceObj, true);
                    } catch (Throwable ignored) {
                        Method m = serviceObj.getClass().getMethod("next");
                        m.invoke(serviceObj);
                    }
                } else if ("Backward".equals(actionSuffix)) {
                    try {
                        Method m = serviceObj.getClass().getMethod("previous", boolean.class);
                        m.invoke(serviceObj, false);
                    } catch (Throwable ignored) {
                        Method m = serviceObj.getClass().getMethod("previous");
                        m.invoke(serviceObj);
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    private static int getVideoJumpDelay() {
        try {
            Class<?> settingsClass = Class.forName("org.videolan.tools.Settings");
            Field instanceField = settingsClass.getField("INSTANCE");
            Object instance = instanceField.get(null);
            Method m = settingsClass.getMethod("getVideoJumpDelay");
            Object res = m.invoke(instance);
            if (res instanceof Integer && ((Integer) res) > 0) {
                return (Integer) res;
            }
        } catch (Throwable ignored) {}
        return 10;
    }

    private static int getVideoDoubleTapJumpDelay() {
        try {
            Class<?> settingsClass = Class.forName("org.videolan.tools.Settings");
            Field instanceField = settingsClass.getField("INSTANCE");
            Object instance = instanceField.get(null);
            Method m = settingsClass.getMethod("getVideoDoubleTapJumpDelay");
            Object res = m.invoke(instance);
            if (res instanceof Integer && ((Integer) res) > 0) {
                return (Integer) res;
            }
        } catch (Throwable ignored) {}
        return 10;
    }

    private static int getDrawableId(Context context, String name, int fallback) {
        int id = getIdentifier(context, name, "drawable");
        return id != 0 ? id : fallback;
    }

    private static int getIdentifier(Context context, String name, String defType) {
        try {
            int id = context.getResources().getIdentifier(name, defType, context.getPackageName());
            if (id != 0) return id;
            return context.getResources().getIdentifier(name, defType, "org.videolan.vlc");
        } catch (Throwable ignored) {}
        return 0;
    }

    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
