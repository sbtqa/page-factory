package ru.sbtqa.tag.pagefactory.support;

import ru.sbtqa.tag.allurehelper.ParamsHelper;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.videorecorder.VideoRecorder;
import ru.yandex.qatools.allure.cucumberjvm.callback.OnFailureCallback;

public class OnFailureScheduler implements OnFailureCallback {

    @Override
    public Object call() {
        ScreenShooter.take();
        return null;
    }
}
