package app.kodex.extension;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
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
    private static long lastDoubleTapTime = 0L;

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

            // Native VLC player icons from normal video playing mode
            int rewindIconId = getDrawableId(context, "ic_player_rewind_10",
                    getDrawableId(context, "ic_widget_rewind_10", android.R.drawable.ic_media_previous));
            int forwardIconId = getDrawableId(context, "ic_player_forward_10",
                    getDrawableId(context, "ic_widget_forward_10", android.R.drawable.ic_media_next));
            int nextIconId = getDrawableId(context, "ic_player_next",
                    getDrawableId(context, "ic_notif_next", android.R.drawable.ic_media_next));

            // Check if playing to pick play vs pause player icon
            boolean isPlaying = true;
            try {
                Field serviceField = context.getClass().getDeclaredField("service");
                serviceField.setAccessible(true);
                Object serviceObj = serviceField.get(context);
                if (serviceObj != null) {
                    Method isPlayingMethod = serviceObj.getClass().getMethod("isPlaying");
                    isPlaying = (Boolean) isPlayingMethod.invoke(serviceObj);
                }
            } catch (Throwable ignored) {}

            int playPauseIconId;
            String playPauseTitle;
            if (isPlaying) {
                playPauseIconId = getDrawableId(context, "ic_pause_player",
                        getDrawableId(context, "ic_popup_pause", android.R.drawable.ic_media_pause));
                playPauseTitle = "Pause";
            } else {
                playPauseIconId = getDrawableId(context, "ic_play_player",
                        getDrawableId(context, "ic_popup_play", android.R.drawable.ic_media_play));
                playPauseTitle = "Play";
            }

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
                    playPauseTitle,
                    playPauseTitle,
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
        long now = System.currentTimeMillis();
        if (now - lastDoubleTapTime < 250L) {
            return;
        }
        lastDoubleTapTime = now;

        try {
            Field serviceField = popupManagerObj.getClass().getDeclaredField("service");
            serviceField.setAccessible(true);
            Object serviceObj = serviceField.get(popupManagerObj);

            Field rootViewField = popupManagerObj.getClass().getDeclaredField("rootView");
            rootViewField.setAccessible(true);
            View rootView = (View) rootViewField.get(popupManagerObj);

            float touchX = event.getX();
            int width = 0;
            if (rootView != null) {
                width = rootView.getWidth();
                try {
                    int[] loc = new int[2];
                    rootView.getLocationOnScreen(loc);
                    float localX = event.getRawX() - loc[0];
                    if (localX > 0 && localX < width) {
                        touchX = localX;
                    }
                } catch (Throwable ignored) {}
            }
            if (width <= 0) width = 1000;

            long jumpDelay = getVideoDoubleTapJumpDelay();
            long deltaMs = jumpDelay * 1000L;
            int direction = (touchX < width / 2.0f) ? -1 : 1;

            seekService(serviceObj, deltaMs * direction);
        } catch (Throwable t) {
            Log.e(TAG, "Failed onPopupDoubleTap", t);
        }
    }

    // -------------------------------------------------------------
    // 3. Floating Pop-up Player: Initialization & Dedicated Buttons
    // -------------------------------------------------------------

    public static void initPopup(View rootView, Object vlcVout, final Object popupManagerObj) {
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

        // Attach direct gesture detector to surface to ensure double-tap is always caught
        if (rootView != null && popupManagerObj != null) {
            try {
                Context ctx = rootView.getContext();
                final GestureDetector surfaceGestureDetector = new GestureDetector(ctx,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }

                            @Override
                            public boolean onDoubleTap(MotionEvent e) {
                                onPopupDoubleTap(popupManagerObj, e);
                                return true;
                            }
                        });

                int surfaceId = getIdentifier(ctx, "player_surface", "id");
                final View surface = surfaceId != 0 ? rootView.findViewById(surfaceId) : null;
                if (surface != null) {
                    surface.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            surfaceGestureDetector.onTouchEvent(event);
                            return false; // let PopupLayout handle drag/scaling
                        }
                    });
                }
            } catch (Throwable t) {
                Log.e(TAG, "Failed to attach surface double-tap detector", t);
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

                    int iconSize = dpToPx(context, 36);
                    int iconPadding = dpToPx(context, 4);
                    int gap = dpToPx(context, 8);

                    // Normal player HUD icons
                    int prevIconId = getDrawableId(context, "ic_player_previous",
                            getDrawableId(context, "ic_notif_previous", android.R.drawable.ic_media_previous));
                    int rewindIconId = getDrawableId(context, "ic_player_rewind_10",
                            getDrawableId(context, "ic_widget_rewind_10", android.R.drawable.ic_media_previous));
                    final int playIconId = getDrawableId(context, "ic_play_player",
                            getDrawableId(context, "ic_popup_play", android.R.drawable.ic_media_play));
                    final int pauseIconId = getDrawableId(context, "ic_pause_player",
                            getDrawableId(context, "ic_popup_pause", android.R.drawable.ic_media_pause));
                    int forwardIconId = getDrawableId(context, "ic_player_forward_10",
                            getDrawableId(context, "ic_widget_forward_10", android.R.drawable.ic_media_next));
                    int nextIconId = getDrawableId(context, "ic_player_next",
                            getDrawableId(context, "ic_notif_next", android.R.drawable.ic_media_next));

                    // Prev Button
                    ImageView prevBtn = new ImageView(context);
                    prevBtn.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
                    prevBtn.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
                    prevBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    prevBtn.setImageResource(prevIconId);
                    prevBtn.setColorFilter(Color.WHITE);
                    applyBorderlessRipple(context, prevBtn);
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
                    rewindBtn.setImageResource(rewindIconId);
                    rewindBtn.setColorFilter(Color.WHITE);
                    applyBorderlessRipple(context, rewindBtn);
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
                    forwardBtn.setImageResource(forwardIconId);
                    forwardBtn.setColorFilter(Color.WHITE);
                    applyBorderlessRipple(context, forwardBtn);
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
                    nextBtn.setImageResource(nextIconId);
                    nextBtn.setColorFilter(Color.WHITE);
                    applyBorderlessRipple(context, nextBtn);
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

                    // Ensure playPauseView is styled and tinted to match player HUD
                    if (playPauseView instanceof ImageView) {
                        ((ImageView) playPauseView).setColorFilter(Color.WHITE);
                    }

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

                    // Keep controls visibility and play/pause icon synchronized with service state
                    playPauseView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            int vis = playPauseView.getVisibility();
                            if (controlsRow.getVisibility() != vis) {
                                controlsRow.setVisibility(vis);
                            }

                            if (playPauseView instanceof ImageView) {
                                boolean isPlaying = false;
                                try {
                                    Method isPlayingMethod = serviceObj.getClass().getMethod("isPlaying");
                                    isPlaying = (Boolean) isPlayingMethod.invoke(serviceObj);
                                } catch (Throwable ignored) {}
                                ((ImageView) playPauseView).setImageResource(isPlaying ? pauseIconId : playIconId);
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

    public static void seekService(Object serviceObj, long deltaMs) {
        if (serviceObj == null) return;
        try {
            long currentTime = 0L;
            try {
                Method getTimeMethod = serviceObj.getClass().getMethod("getTime");
                currentTime = (Long) getTimeMethod.invoke(serviceObj);
            } catch (Throwable ignored) {}

            long targetTime = Math.max(0L, currentTime + deltaMs);

            // 1. Direct seek call on service
            boolean seekInvoked = false;
            try {
                Method seekMethod = serviceObj.getClass().getMethod("seek", long.class);
                seekMethod.invoke(serviceObj, targetTime);
                seekInvoked = true;
            } catch (Throwable ignored) {}

            if (!seekInvoked) {
                try {
                    Method setTimeMethod = serviceObj.getClass().getMethod("setTime", long.class, boolean.class);
                    setTimeMethod.invoke(serviceObj, targetTime, false);
                    seekInvoked = true;
                } catch (Throwable ignored) {}
            }

            // 2. Direct seek on PlayerController
            try {
                Field pmField = serviceObj.getClass().getDeclaredField("playlistManager");
                pmField.setAccessible(true);
                Object pm = pmField.get(serviceObj);
                Field playerField = pm.getClass().getDeclaredField("player");
                playerField.setAccessible(true);
                Object player = playerField.get(pm);

                Method setTimePlayer = player.getClass().getMethod("setTime", long.class, boolean.class);
                setTimePlayer.invoke(player, targetTime, false);

                Method updateProgress = player.getClass().getMethod("updateProgress", long.class);
                updateProgress.invoke(player, targetTime);
            } catch (Throwable ignored) {}

            // 3. Fallback: Send VLC's standard remote seek intent
            if (serviceObj instanceof Context) {
                Context ctx = (Context) serviceObj;
                String pkg = ctx.getPackageName();
                if (pkg == null) pkg = "org.videolan.vlc";
                String actionSuffix = deltaMs >= 0 ? "SeekForward" : "SeekBackward";
                Intent intent = new Intent(pkg + ".remote." + actionSuffix);
                intent.setComponent(new ComponentName(ctx, "org.videolan.vlc.PlaybackService"));
                long delaySec = Math.abs(deltaMs) / 1000L;
                if (delaySec <= 0) delaySec = 10L;
                intent.putExtra(pkg + ".EXTRA_CUSTOM_ACTION_ID", delaySec);
                intent.putExtra("EXTRA_CUSTOM_ACTION_ID", delaySec);
                ctx.startService(intent);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error in seekService", t);
        }
    }

    private static void sendPlaybackAction(Context context, Object serviceObj, String actionSuffix) {
        try {
            String pkg = context != null ? context.getPackageName() : "org.videolan.vlc";
            if (pkg == null) pkg = "org.videolan.vlc";
            String action = pkg + ".remote." + actionSuffix;
            Intent intent = new Intent(action);
            intent.setComponent(new ComponentName(context, "org.videolan.vlc.PlaybackService"));
            context.startService(intent);
        } catch (Throwable ignored) {}

        if (serviceObj != null) {
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

    private static void applyBorderlessRipple(Context context, View view) {
        try {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            view.setBackgroundResource(outValue.resourceId);
        } catch (Throwable ignored) {}
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
