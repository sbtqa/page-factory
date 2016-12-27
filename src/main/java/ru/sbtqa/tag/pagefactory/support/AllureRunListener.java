package ru.sbtqa.tag.pagefactory.support;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.allurehelper.AllureNonCriticalFailure;
import ru.sbtqa.tag.qautils.properties.Props;

public class AllureRunListener extends ru.sbtqa.tag.allurehelper.TagAllureRunListener {

    private static final Logger LOG = LoggerFactory.getLogger(AllureRunListener.class);

    /**
     *
     * @param failure TODO
     */
    @Override
    public void testFailure(Failure failure) {
        
//        if (PageFactory.getVideoRecorder() != null && PageFactory.getVideoRecorder().isVideoStarted()) {
//            String videoPath = PageFactory.getVideoRecorder().stopRecording();
//            if (videoPath != null) {
//                addVideoParameter(PageFactory.getVideoRecorder().getvideoPath());
//                PageFactory.setVideoRecorderToNull();
//            }
//        }
        LOG.debug("TestFailure:" + failure.getTrace());
        
        takeScreenshot();
 
        super.testFailure(failure);
    }

    /**
     * Mark test as Failure for Allure report if test failed, but it was not critical
     *
     * @param description - description of test
     * @throws IllegalAccessException TODO
     */
    @Override
    public void testFinished(Description description) throws IllegalAccessException {
//        if (PageFactory.getVideoRecorder() != null) {
//            addVideoParameter(PageFactory.getVideoRecorder().getvideoPath());
//        }
        
        if (AllureNonCriticalFailure.getFailure().containsKey(Thread.currentThread())) {
            takeScreenshot();
        }
        
        super.testFinished(description);
    }

    /**
     *
     * @param uid TODO
     */
    @Override
    public void testSuiteFinished(String uid) {
//        if (PageFactory.getVideoRecorder() != null && PageFactory.getVideoRecorder().isVideoStarted()) {
//            String videoPath = PageFactory.getVideoRecorder().stopRecording();
//            if (videoPath != null) {
//                addVideoParameter(PageFactory.getVideoRecorder().getvideoPath());
//                PageFactory.setVideoRecorderToNull();
//            }
//        }
        
        super.testSuiteFinished(uid);
    }
    
    /**
     *
     */
    public static void takeScreenshot() {
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
