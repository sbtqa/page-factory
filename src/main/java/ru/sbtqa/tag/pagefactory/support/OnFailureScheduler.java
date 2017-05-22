package ru.sbtqa.tag.pagefactory.support;

import ru.yandex.qatools.allure.cucumberjvm.callback.OnFailureCallback;

public class OnFailureScheduler implements OnFailureCallback {

    @Override
    public Object call() {
        ScreenShooter.take();
        return null;
    }
}
