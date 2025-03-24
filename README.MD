## Запуск программ

### Для работы требуется прописанный в окружении MPJ_HOME

### Команды для запуска:
### LAB 1
Для каждой цифры (от 2 до 10) девять множителей (от 2 до 10)

Всего 81 результат
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -cp ".\target\classes" neko.lab1.part1
```

### LAB 2

#### part1
Обмен данными между процессами
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -cp ".\target\classes" neko.lab2.part1
```

#### part2
Сумма нечетных элементов вектора
- `-Dvector="5 5 -5"`: Входной вектор (только целые значения)
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -Dvector="5 5 -5" -cp ".\target\classes" neko.lab2.part2
```


### LAB 3
Заменить в прямоугольной матрице все положительные элементы на 1, отрицательные на -1;

Количество элементов, оставшихся неизменными в новой матрице.


- `-Dmatrix="-1 -2 3 4;-5 6 -7 0;9 -10 -11 1"`: Входная матрица (только целые значения)
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -Dmatrix="-1 -2 3 4;-5 6 -7 0;9 -10 -11 1" -cp ".\target\classes" neko.lab3.part1
```


### LAB 4

#### part1
Решить задание 2 из лабораторной работы 2 с применением коллективных функций
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -Dvector="5 5 -5 3 4 -2 5 3 5 -9 2 4" -cp ".\target\classes" neko.lab4.part1
```

#### part2
Решить задание 1 и 2 из лабораторной работы 3 с применением коллективных функций.
- `-Dmatrix="-1 -2 3 4;-5 6 -7 0;9 -10 -11 1"`: Входная матрица (только целые значения)
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 4 -Dmatrix="-1 -2 3 4;-5 6 -7 0;9 -10 -11 1" -cp ".\target\classes" neko.lab4.part2
```

### LAB 5

Матрицы хранятся в файле test_matrix.json

Есть возможность включить подробные логи в каждом из файлов (boolean logs = true/false)

#### BlockDiagonalMatrixBase
Последовательный алгоритм преобразования разреженной матрицы в блочно-диагональную
```bash
java -cp ".\json-simple\1.1\json-simple-1.1.jar;.\target\classes" neko.lab5.BlockDiagonalMatrixBase
```

#### BlockDiagonalMatrixParallel
Параллельный алгоритм преобразования разреженной матрицы в блочно-диагональную
```bash
java -jar ".\mpj-v0_44\lib\starter.jar" -np 2 -cp ".\json-simple\1.1\json-simple-1.1.jar;.\target\classes" neko.lab5.BlockDiagonalMatrixParallel
```