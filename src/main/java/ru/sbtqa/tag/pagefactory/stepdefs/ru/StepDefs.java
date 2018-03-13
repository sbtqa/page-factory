package ru.sbtqa.tag.pagefactory.stepdefs.ru;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import java.util.List;
import org.openqa.selenium.NoSuchElementException;
import ru.sbtqa.tag.pagefactory.exceptions.PageException;
import ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException;
import ru.sbtqa.tag.pagefactory.exceptions.SwipeException;
import ru.sbtqa.tag.pagefactory.stepdefs.GenericStepDefs;

public class StepDefs extends GenericStepDefs {
    
    @Override
    @And("^(?:пользователь |он |)в блоке \"([^\"]*)\" \\((.*?)\\)$")
    public void userActionInBlockNoParams(String block, String action) throws PageInitializationException,
            NoSuchMethodException, NoSuchElementException {
        super.userActionInBlockNoParams(block, action);
    }

    @Override
    @And("^(?:пользователь |он |)в блоке \"([^\"]*)\" \\((.*?)\\) с параметрами из таблицы$")
    public void userActionInBlockTableParam(String block, String action, DataTable dataTable) throws PageInitializationException, NoSuchMethodException {
       super.userActionInBlockTableParam(block, action, dataTable);
    }

    @Override
    @And("^(?:пользователь |он |)в блоке \"([^\"]*)\" \\((.*?)\\) с параметром \"([^\"]*)\"$")
    public void userActionInBlockOneParam(String block, String action, String param) throws PageInitializationException, NoSuchMethodException {
        super.userActionInBlockOneParam(block, action, param);
    }

    @Override
    @And("^(?:пользователь |он |)в блоке \"([^\"]*)\" \\((.*?)\\) с параметрами \"([^\"]*)\" \"([^\"]*)\"$")
    public void userActionInBlockTwoParams(String block, String action, String param1, String param2) throws PageInitializationException, NoSuchMethodException {
        super.userActionInBlockTwoParams(block, action, param1, param2);
    }

    @Override
    @And("^(?:пользователь |он |)в блоке \"([^\"]*)\" находит (элемент|текстовое поле|чекбокс|радиокнопка|таблицу|заголовок|кнопку|ссылку|изображение) \"([^\"]*)\"$")
    public void findElementInBlock(String block, String elementType, String elementTitle) throws PageException {
        super.findElementInBlock(block, elementType, elementTitle);
    }

    @Override
    @And("^(?:пользователь |он |)в списке \"([^\"]*)\" находит элемент со значением \"([^\"]*)\"$")
    public void findElementInList(String listTitle, String value) throws PageException {
        super.findElementInList(listTitle, value);
    }

    @Override
    @And("^(?:пользователь |он |)(?:находится на странице|открывается страница|открывается вкладка мастера) \"([^\"]*)\"$")
    public void openPage(String title) throws PageInitializationException {
        super.openPage(title);
    }

    @Override
    @And("^(?:пользователь |он |)\\((.*?)\\)$")
    public void userActionNoParams(String action) throws PageInitializationException, NoSuchMethodException {
        super.userActionNoParams(action);
    }

    @Override
    @And("^(?:пользователь |он |)\\((.*?)\\) (?:с параметром |)\"([^\"]*)\"$")
    public void userActionOneParam(String action, String param) throws PageInitializationException, NoSuchMethodException {
        super.userActionOneParam(action, param);
    }

    @Override
    @And("^(?:пользователь |он |)\\((.*?)\\) (?:с параметрами |)\"([^\"]*)\" \"([^\"]*)\"$")
    public void userActionTwoParams(String action, String param1, String param2) throws PageInitializationException, NoSuchMethodException {
        super.userActionTwoParams(action, param1, param2);
    }

    @Override
    @And("^(?:пользователь |он |)\\((.*?)\\) (?:с параметрами |)\"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void userActionThreeParams(String action, String param1, String param2, String param3) throws PageInitializationException, NoSuchMethodException {
        super.userActionThreeParams(action, param1, param2, param3);
    }

    @Override
    @And("^(?:пользователь |он |)\\((.*?)\\) данными$")
    public void userActionTableParam(String action, DataTable dataTable) throws PageInitializationException, NoSuchMethodException {
        super.userActionTableParam(action, dataTable);
    }

    @Override
    @And("^(?:пользователь |он |)\\((.*?)\\) [^\"]*\"([^\"]*)\" данными$")
    public void userDoActionWithObject(String action, String param, DataTable dataTable) throws PageInitializationException, NoSuchMethodException {
        super.userDoActionWithObject(action, param, dataTable);
    }

    @Override
    @And("^(?:пользователь |он |)\\((.*?)\\) из списка$")
    public void userActionListParam(String action, List<String> list) throws PageInitializationException, NoSuchMethodException {
        super.userActionListParam(action, list);
    }

    @Override
    @And("^открывается копия страницы в новой вкладке$")
    public void openCopyPage() {
        super.openCopyPage();
    }

    @Override
    @And("^(?:пользователь |он |)переключается на соседнюю вкладку$")
    public void switchesToNextTab() {
        super.switchesToNextTab();
    }

    @Override
    @And("^URL соответствует \"(.*?)\"$")
    public void urlMatches(String url) {
        super.urlMatches(url);
    }

    @Override
    @And("^(?:пользователь |он |)закрывает текущее окно и возвращается на \"(.*?)\"$")
    public void closingCurrentWin(String title) {
        super.closingCurrentWin(title);
    }

    @Override
    @And("^(?:пользователь |он |)нажимает назад в браузере$")
    public void backPage() {
        super.backPage();
    }

    @Override
    @And("^(?:пользователь |он |)переходит на страницу \"(.*?)\" по ссылке$")
    public void goToUrl(String url) {
        super.goToUrl(url);
    }

    @Override
    @And("^(?:пользователь |он |)(?:переходит на|открывает) url \"(.*?)\"$")
    public void goToPageByUrl(String url) throws PageInitializationException {
        super.goToPageByUrl(url);
    }

    @Override
    @And("^обновляем страницу$")
    public void reInitPage() {
        super.reInitPage();
    }

    @Override
    @And("^пользователь свайпает экран \"([^\"]*)\" до текста \"([^\"]*)\"$")
    public void swipeToText(String direction, String text) throws SwipeException {
        super.swipeToText(direction, text);
    }

    @Override
    @And("^в фокусе находится элемент \"([^\"]*)\"$")
    public void isElementFocused(String element) {
        super.isElementFocused(element);
    }
}