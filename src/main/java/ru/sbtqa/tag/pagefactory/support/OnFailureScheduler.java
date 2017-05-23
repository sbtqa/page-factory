package ru.sbtqa.tag.pagefactory.support;

import ru.sbtqa.tag.allurehelper.ParamsHelper;
import ru.sbtqa.tag.allurehelper.Type;
import ru.yandex.qatools.allure.cucumberjvm.callback.OnFailureCallback;

public class OnFailureScheduler implements OnFailureCallback {

    @Override
    public Object call() {
        ParamsHelper.addAttachment(ScreenShooter.take(), "Screenshot", Type.PNG);
        return null;
    }
}
