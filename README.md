<h1 align="center">Привет! Я <a href="https://daniilshat.ru/" target="_blank">Анастасия</a> 
<img src="https://github.com/blackcater/blackcater/raw/main/images/Hi.gif" height="32"/></h1>
<h3 align="center">Студентка онлайн-университета Skillbox. Это мой итоговый проект к базовому курсу обучения разработки на Java.</h3>
Поисковый движок для поиска информации по заранее определенным сайтам.

![2024-03-20_15-25-39](https://github.com/AnastasiyaVetrova/SearchEngine/assets/145038107/63a2c3da-6cd3-4d2e-aa9e-6d7c1bbecdb9)

Позволяет получать информацию о всех страницах сайта в формате HTML.

![2024-03-20_15-31-57](https://github.com/AnastasiyaVetrova/SearchEngine/assets/145038107/5283bd13-2b4c-4580-b877-a4a9067d0a1d)

  Список сайтов находится в конфигурационном файле. Движок может индексировать как полностью все сайты, так и отдельные страницы.
  
  ![2024-03-20_17-49-41](https://github.com/AnastasiyaVetrova/SearchEngine/assets/145038107/962b7186-b94c-4a25-8601-c81c1b99b4eb)


  Поиск может происходить также по отдельным сайтам или полностью по всем.
  Движок работает на базе MySql. Впишите в конфигурационном файле данные для подключения к Вашей базе данных.
Проект использует многопоточность ForkJoinPool для более быстрой индексации сразу нескольких сайтов.
В основе кода лежит Spring Framework, зависимости подключены с помощью Maven.
Помимо стандартных библиотек используется используется лематизатор russianmorphology, ссылка на него: 
[russianmorphology](https://github.com/akuznetsov/russianmorphology)https://github.com/akuznetsov/russianmorphology

Поисковый движок имеет высокую точность результатьв, но из за теряет в скорости в части полной индексации сайтов.
