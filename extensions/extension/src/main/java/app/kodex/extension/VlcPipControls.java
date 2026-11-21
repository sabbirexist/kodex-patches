package app.kodex.extension;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class VlcPipControls {

    private static final String ACTION_REMOTE_BACKWARD = "org.videolan.vlc.remote.Backward";
    private static final String ACTION_REMOTE_PLAYPAUSE = "org.videolan.vlc.remote.PlayPause";
    private static final String ACTION_REMOTE_FORWARD = "org.videolan.vlc.remote.Forward";

    private static final int POPUP_PREV_VIEW_ID = 0x7f099901;
    private static final int POPUP_NEXT_VIEW_ID = 0x7f099902;

    @TargetApi(Build.VERSION_CODES.O)
    public static List<RemoteAction> getPipActions(Context context) {
        Object serviceObj = null;
        if (context != null) {
            try {
                Method m = context.getClass().getMethod("getService");
                serviceObj = m.invoke(context);
            } catch (Throwable ignored) {
                try {
                    Field f = context.getClass().getDeclaredField("service");
                    f.setAccessible(true);
                    serviceObj = f.get(context);
                } catch (Throwable ignored2) {}
            }
        }
        return getPipActions(context, serviceObj);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static List<RemoteAction> getPipActions(Context context, Object serviceObj) {
        List<RemoteAction> actions = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context == null) {
            return actions;
        }

        try {
            Class<?> serviceClass = Class.forName("org.videolan.vlc.PlaybackService");

            boolean isPlaying = false;
            boolean hasPrev = true;
            boolean hasNext = true;

            if (serviceObj != null) {
                try {
                    Method isPlayingMethod = serviceClass.getMethod("isPlaying");
                    Object res = isPlayingMethod.invoke(serviceObj);
                    if (res instanceof Boolean) {
                        isPlaying = (Boolean) res;
                    }
                } catch (Throwable ignored) {}

                try {
                    Method hasPrevMethod = serviceClass.getMethod("hasPrevious");
                    Object res = hasPrevMethod.invoke(serviceObj);
                    if (res instanceof Boolean) {
                        hasPrev = (Boolean) res;
                    }
                } catch (Throwable ignored) {}

                try {
                    Method hasNextMethod = serviceClass.getMethod("hasNext");
                    Object res = hasNextMethod.invoke(serviceObj);
                    if (res instanceof Boolean) {
                        hasNext = (Boolean) res;
                    }
                } catch (Throwable ignored) {}
            }

            String pkg = context.getPackageName();
            String backwardAction = pkg != null ? pkg + ".remote.Backward" : ACTION_REMOTE_BACKWARD;
            String playPauseActionStr = pkg != null ? pkg + ".remote.PlayPause" : ACTION_REMOTE_PLAYPAUSE;
            String forwardAction = pkg != null ? pkg + ".remote.Forward" : ACTION_REMOTE_FORWARD;

            // 1. Previous Video Action
            Intent prevIntent = new Intent(backwardAction, null, context, serviceClass);
            PendingIntent piPrev = createPendingIntent(context, serviceObj, prevIntent, 101);
            int prevIconId = getDrawableId(context, "ic_notif_previous", android.R.drawable.ic_media_previous);
            RemoteAction prevAction = new RemoteAction(
                Icon.createWithResource(context, prevIconId),
                "Previous",
                "Previous video",
                piPrev
            );
            prevAction.setEnabled(hasPrev);
            actions.add(prevAction);

            // 2. Play / Pause Action
            Intent playPauseIntent = new Intent(playPauseActionStr, null, context, serviceClass);
            PendingIntent piPlayPause = createPendingIntent(context, serviceObj, playPauseIntent, 102);
            int playPauseIconId = isPlaying
                ? getDrawableId(context, "ic_popup_pause", android.R.drawable.ic_media_pause)
                : getDrawableId(context, "ic_popup_play", android.R.drawable.ic_media_play);
            CharSequence title = isPlaying ? "Pause" : "Play";
            RemoteAction playPauseAction = new RemoteAction(
                Icon.createWithResource(context, playPauseIconId),
                title,
                title,
                piPlayPause
            );
            actions.add(playPauseAction);

            // 3. Next Video Action
            Intent nextIntent = new Intent(forwardAction, null, context, serviceClass);
            PendingIntent piNext = createPendingIntent(context, serviceObj, nextIntent, 103);
            int nextIconId = getDrawableId(context, "ic_notif_next", android.R.drawable.ic_media_next);
            RemoteAction nextAction = new RemoteAction(
                Icon.createWithResource(context, nextIconId),
                "Next",
                "Next video",
                piNext
            );
            nextAction.setEnabled(hasNext);
            actions.add(nextAction);

        } catch (Throwable ignored) {}

        return actions;
    }

    private static PendingIntent createPendingIntent(Context context, Object serviceObj, Intent intent, int requestCode) {
        if (serviceObj != null) {
            try {
                Method m = serviceObj.getClass().getMethod("getPendingIntent", Intent.class);
                Object res = m.invoke(serviceObj, intent);
                if (res instanceof PendingIntent) {
                    return (PendingIntent) res;
                }
            } catch (Throwable ignored) {}
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            return PendingIntent.getService(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }
    }

    private static int getDrawableId(Context ctx, String name, int fallback) {
        try {
            int id = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
            if (id != 0) return id;
            id = ctx.getResources().getIdentifier(name, "drawable", "org.videolan.vlc");
            if (id != 0) return id;
        } catch (Throwable ignored) {}
        return fallback;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void updatePipActions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null && activity.isInPictureInPictureMode()) {
            try {
                PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
                builder.setActions(getPipActions(activity));
                activity.setPictureInPictureParams(builder.build());
            } catch (Throwable ignored) {}
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void updatePipActions(Activity activity, Object serviceObj) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null && activity.isInPictureInPictureMode()) {
            try {
                PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
                builder.setActions(getPipActions(activity, serviceObj));
                activity.setPictureInPictureParams(builder.build());
            } catch (Throwable ignored) {}
        }
    }

    public static void setupPopupButtons(View rootView, final Object serviceObj) {
        if (!(rootView instanceof ViewGroup) || serviceObj == null) return;
        try {
            final Context ctx = rootView.getContext();
            final ViewGroup group = (ViewGroup) rootView;

            int playPauseId = ctx.getResources().getIdentifier("video_play_pause", "id", ctx.getPackageName());
            if (playPauseId == 0) {
                playPauseId = ctx.getResources().getIdentifier("video_play_pause", "id", "org.videolan.vlc");
            }
            View playPauseView = playPauseId != 0 ? group.findViewById(playPauseId) : null;
            if (playPauseView == null) return;

            if (group.findViewById(POPUP_PREV_VIEW_ID) != null) return;

            int prevIcon = getDrawableId(ctx, "ic_notif_previous", android.R.drawable.ic_media_previous);
            int nextIcon = getDrawableId(ctx, "ic_notif_next", android.R.drawable.ic_media_next);

            float density = ctx.getResources().getDisplayMetrics().density;
            int margin = (int) (32 * density);
            int size = (int) (36 * density);

            // Previous Button
            ImageView prevBtn = new ImageView(ctx);
            prevBtn.setId(POPUP_PREV_VIEW_ID);
            prevBtn.setImageResource(prevIcon);
            prevBtn.setScaleType(ImageView.ScaleType.CENTER);
            prevBtn.setVisibility(playPauseView.getVisibility());
            prevBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Method m = serviceObj.getClass().getMethod("previous", boolean.class);
                        m.invoke(serviceObj, true);
                    } catch (Throwable ignored) {
                        try {
                            Method m = serviceObj.getClass().getMethod("previous");
                            m.invoke(serviceObj);
                        } catch (Throwable ignored2) {}
                    }
                }
            });

            // Next Button
            ImageView nextBtn = new ImageView(ctx);
            nextBtn.setId(POPUP_NEXT_VIEW_ID);
            nextBtn.setImageResource(nextIcon);
            nextBtn.setScaleType(ImageView.ScaleType.CENTER);
            nextBtn.setVisibility(playPauseView.getVisibility());
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Method m = serviceObj.getClass().getMethod("next", boolean.class);
                        m.invoke(serviceObj, true);
                    } catch (Throwable ignored) {
                        try {
                            Method m = serviceObj.getClass().getMethod("next");
                            m.invoke(serviceObj);
                        } catch (Throwable ignored2) {}
                    }
                }
            });

            try {
                Class<?> clClass = Class.forName("androidx.constraintlayout.widget.ConstraintLayout");
                if (clClass.isInstance(rootView)) {
                    Class<?> lpClass = Class.forName("androidx.constraintlayout.widget.ConstraintLayout$LayoutParams");
                    ViewGroup.LayoutParams lpPrev = (ViewGroup.LayoutParams) lpClass.getConstructor(int.class, int.class).newInstance(size, size);
                    lpClass.getField("endToStart").setInt(lpPrev, playPauseId);
                    lpClass.getField("topToTop").setInt(lpPrev, playPauseId);
                    lpClass.getField("bottomToBottom").setInt(lpPrev, playPauseId);
                    lpClass.getField("rightMargin").setInt(lpPrev, margin);
                    group.addView(prevBtn, lpPrev);

                    ViewGroup.LayoutParams lpNext = (ViewGroup.LayoutParams) lpClass.getConstructor(int.class, int.class).newInstance(size, size);
                    lpClass.getField("startToEnd").setInt(lpNext, playPauseId);
                    lpClass.getField("topToTop").setInt(lpNext, playPauseId);
                    lpClass.getField("bottomToBottom").setInt(lpNext, playPauseId);
                    lpClass.getField("leftMargin").setInt(lpNext, margin);
                    group.addView(nextBtn, lpNext);
                    return;
                }
            } catch (Throwable ignored) {}

            group.addView(prevBtn);
            group.addView(nextBtn);
        } catch (Throwable ignored) {}
    }

    public static void setPopupButtonsVisibility(View rootView, int visibility) {
        if (!(rootView instanceof ViewGroup)) return;
        try {
            ViewGroup group = (ViewGroup) rootView;
            View prevBtn = group.findViewById(POPUP_PREV_VIEW_ID);
            View nextBtn = group.findViewById(POPUP_NEXT_VIEW_ID);
            if (prevBtn != null) prevBtn.setVisibility(visibility);
            if (nextBtn != null) nextBtn.setVisibility(visibility);
        } catch (Throwable ignored) {}
    }

    public static void onPopupHandleMessage(View rootView, int msgWhat) {
        int visibility = (msgWhat == 0) ? View.VISIBLE : View.GONE;
        setPopupButtonsVisibility(rootView, visibility);
    }

    public static Object mapPlaybackAction(Object action) {
        if (action != null && "PLAY".equals(((Enum<?>) action).name())) {
            try {
                Class<?> enumClass = action.getClass();
                for (Object constant : enumClass.getEnumConstants()) {
                    if ("PLAY_ALL".equals(((Enum<?>) constant).name())) {
                        return constant;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return action;
    }
}
