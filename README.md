# Page-Factory
[![Build Status](https://travis-ci.org/sbtqa/page-factory.svg?branch=master)](https://travis-ci.org/sbtqa/page-factory) [![GitHub release](https://img.shields.io/github/release/sbtqa/page-factory.svg?style=flat-square)](https://github.com/sbtqa/page-factory/releases) [![Maven Central](https://img.shields.io/maven-central/v/ru.sbtqa.tag/page-factory.svg)](https://mvnrepository.com/artifact/ru.sbtqa.tag/page-factory)

Page-Factory это opensource java framework для автоматизированного тестирования, который позволяет разрабатывать автотесты в BDD(Behaviour Driven Development) стиле с акцентом на использование паттерна PageFactory. 

### О Page-Factory

Page-Factory позволяет писать автотесты на человекочитаемом языке, тем самым понижая входной порог для разработчиков тестов и повышая читаемость автотестов неподготовлеными пользователями. Page-factory использует framework Cucumber, но в отличии от чистого использования Cucumber в котором довольно большую часть архитектуры занимают шаги(steps), в Page-factory акцент сделан на то чтобы сократить количество шагов и сосредоточится на описании кода страниц с использованием паттерна PageFactory и фич на языке Gherkin.   
В Page-Factory уже реализовано много стандартных шагов(steps), которых хватит чтобы начать разрабатывать свои автоматизированные тесты. 
Page-Factory кроссплатформенный фреймворк который позволяет запускать тесты на всех популярных браузерах, потому что для их запуска используется Selenium WebDriver. Так же Page-Factory умеет работать с приложения на Android средствами Appium.

### Требования
Для работы Page-Factory нужно:
1. Java 7 или выше

### Документация
Начать пользоваться page-factory очень просто, можно начать с подготовленных [примеров](https://github.com/sbtqa/page-factory-example) или воспользоваться [документацией](https://github.com/sbtqa/docs/wiki/Page-Factory)

### Контакты
Нашли ошибку или появились вопросы? [Проверьте](https://github.com/sbtqa/page-factory/issues) нет ли уже созданных issue, если нет то создайте [новое](https://github.com/sbtqa/page-factory/issues/new)!

### Лицензия 
Page-Factory выпущен под лицензией Apache 2.0. [Подробности](https://github.com/sbtqa/page-factory/blob/master/LICENSE).
