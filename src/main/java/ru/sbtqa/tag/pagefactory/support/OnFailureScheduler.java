package ru.sbtqa.tag.pagefactory.support;

import ru.sbtqa.tag.allurehelper.ParamsHelper;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.videorecorder.VideoRecorder;
import ru.yandex.qatools.allure.cucumberjvm.callback.OnFailureCallback;

public class OnFailureScheduler implements OnFailureCallback {

    public static void addVideoParameter(String videoPath) {
        ParamsHelper.addParam("Video url", videoPath);
    }

    private void takeScreenshot() {
        String screenshotStrategy = Props.get("screenshot.strategy", "raw");

        switch (screenshotStrategy) {
            case "driver":
                ScreenShooter.takeWithDriver();
                break;
            case "raw":
            default:
                ScreenShooter.takeRaw();
                break;
        }
    }

    @Override
    public Object call() {
        if (VideoRecorder.getInstance().isVideoStarted()) {
            String videoPath = VideoRecorder.getInstance().stopRecording();
            if (videoPath != null) {
                addVideoParameter(VideoRecorder.getInstance().getVideoPath());
                VideoRecorder.getInstance().resetVideoRecorder();
            }
        }
        takeScreenshot();
        return null;
    }
}
