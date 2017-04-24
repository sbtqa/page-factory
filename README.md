#Page-Factory
[![Build Status](https://travis-ci.org/sbtqa/page-factory.svg?branch=master)](https://travis-ci.org/sbtqa/page-factory) [![GitHub release](https://img.shields.io/github/release/sbtqa/page-factory.svg?style=flat-square)](https://github.com/sbtqa/page-factory/releases)

Page-Factory это opensource java framework для автоматизированного тестирования, который позволяет разрабатывать автотесты в BDD(Behaviour Driven Development) стиле с акцентом на использование паттерна PageFactory. 

### Почему Page-Factory

&ensp;Page-Factory позволяет очень быстро начать писать тесты в BDD стиле используя Gherkin. Одной из самых главных преимуществ фреймворка является, то что мы концентрируемся на том чтобы уменьшить количество шагов и сконцентрироваться на создании страниц с использованием паттерна PageFactory.  В Page-Factory уже реализовано много стандартных шагов(steps). Page-Factory позволяет запускать тесты на всех популярных браузерах, потому что для их запуска используется Selenium WebDriver. 

###Что внутри
Page-Factory строится на использовании следующих инструментов:

 1. Selenium WebDriver 
 2. Cucumber
 3. Allure Report

###Быстрый старт
&ensp;Чтобы подключить PageFactory в свой Maven проект достаточно в файл pom добавить 

    <dependency>
	    <groupId>ru.sbtqa.tag</groupId>
	    <artifactId>page-factory</artifactId>
	    <version>1.3.8</version>
	</dependency>

&ensp;Дальше просто создайте feature и реализуйте страницу(page) для своего приложения.
&ensp;Вы можете скачать примеры тестового проекта, чтобы еще быстрее начать работать с Page-Factory. Примеры [здесь](http://github.com/sbtqa/page-factory-example)

&ensp;Более подробное описание работы есть в [wiki](https://github.com/sbtqa/page-factory/wiki)

###Контакты
Нашли ошибку или появились вопросы? Создайте [новое issue](https://github.com/sbtqa/page-factory/issues/new)!

###Лицензия 
PageFactory выпущен под лицензией Apache 2.0. Подробности.