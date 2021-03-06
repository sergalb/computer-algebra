## Реферат
Описание алгоритма Диксона и метода сопряженных векторов для решения системы линейных уравнений над конечным полем Галуа, в файле computer_algebra.pdf
## Реализация метода в системе компьютерной алгебре maple
Файл среды компьютерной алгебры solution.mv лежит в директории dixon-maple. Алгоритм аналогичен решению на Kotlin, однако без использования разреженной матрицы.
## Реализация на языке программирования (Kotlin)
Созданы классы Matrix и SparseMatrix 
* В классе Matrix реализованы методы solveHomogeneousSLE для решения однородной СЛАУ методом Гаусса над конечным полем. Этот метод использовался для теста алгоритма Диксона и его отладки;
метод conjugateGradientMethod, который решает СЛАУ при помощи метода сопряженных градиентов, однако на матрице, которая хранится в полном виде
* Класс SparseMatrix хранит разреженную матрицу, как список ненулевых значений с координатами. Были реализованы основные матричные функции в конечном поле по заданному основанию, а также conjugateGradientMethod, аналогичный методу из Matrix.\
В файле Solution реализована функция factorize и вспомогательные методы для алгоритма Диксона.
В процессе реализации выяснилось, что наибольшее время работы алгоритма Диксона занимает процесс генерации B-гладких чисел, а способ решения СЛАУ хоть и влияет на время, но незначительно
  
Так как алгоритм случайный, то количество попыток поиска делителей ограниченно десятью.
## Тесты и производительность: 
В директории dixon-with-cg-kotlin/src/test/kotlin/DixonTests написаны следующие тесты:
* Позитивные - на то что составные числа должны факторизоваться (в скобках время выполнения):
  * 89755 (1мс)
  * 12916811 (8мс)
  * 16769021 (2мс)
  * 507081261 (10мс)
  * 1125899839733757(4с, 68мс)
* Негативные - простые числа не должны факторизоваться
  * 17
  * 1125899839733759 
