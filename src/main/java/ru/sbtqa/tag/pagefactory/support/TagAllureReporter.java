package ru.sbtqa.tag.pagefactory.support;

import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.allurehelper.OnFailureScheduler;
import ru.sbtqa.tag.allurehelper.ParamsHelper;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.videorecorder.VideoRecorder;

public class TagAllureReporter extends ru.yandex.qatools.allure.cucumberjvm.AllureReporter {

    private static final Logger LOG = LoggerFactory.getLogger(TagAllureReporter.class);
    private final String uuid = UUID.randomUUID().toString();

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        TagAllureReporter eqCandidate = (TagAllureReporter) obj;
        return eqCandidate.uuid.equals(this.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.uuid);
    }

    @Override
    public void after(Match match, Result result) {
        if (VideoRecorder.getInstance().isVideoStarted()) {
            String videoPath = VideoRecorder.getInstance().stopRecording();
            if (videoPath != null) {
                addVideoParameter(VideoRecorder.getInstance().getVideoPath());
                VideoRecorder.getInstance().resetVideoRecorder();
            }
        }
        if (Result.FAILED.equals(result.getStatus())) {
            LOG.debug(result.getErrorMessage(), result.getError());
            new OnFailureScheduler().processPendings();
        }
    }

    @Override
    public void result(Result result) {
        super.result(result);
        if (Result.FAILED.equals(result.getStatus())) {
            takeScreenshot();
        }
    }

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
}
