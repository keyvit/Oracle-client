package ru.nsu.fit.g14201.marchenko;

/**
 *
 */
public class OracleClientApp
{
    public static void main( String[] args )
    {
        Controller controller = new Controller();
        controller.init();

        //TODO Разделить интерфейсы
        //TODO Озадачиться нормальным закрытием соединения

        //Проблемы с флагами, нулями и какими-то восьмеричными цифрами

        //FIXME Проблема с шириной при вставке новых элементов
        //TODO Сделать выбор типа данных детальнее

        //TODO Drop constraint

        //TODO Обновлять tablePanel после удаления/добавления столбцов
    }
}
