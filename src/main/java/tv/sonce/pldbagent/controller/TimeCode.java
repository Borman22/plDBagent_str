package tv.sonce.pldbagent.controller;

public class TimeCode {
    private int TCInFrame; // в кадрах 23*90_000 + 59*1500 + 59*25 + 24
    private String TCStr; // с разделителями ':'  "23:59:59:24"
    private int TCIntStr; // без разделителей  23595924
    private String [] TCArrayStr = {"00", "00", "00", "00"}; // массив строк {23, 59, 59, 24}
    private int [] TCArrayInt = {0, 0, 0, 0}; // массив интов {23, 59, 59, 24}
    private final int MAX_VALUE = 24*90000; //2_160_000 кадров
    final int MIN_VALUE = 0;

    TimeCode(){
        setTC(0);
    } // Конструктор без параметров

    /**
     * Конструктор получает ТС в виде String ТОЛЬКО в таком формате (11 символов с разделителями-двоеточиями):     23:59:59:24
     */
    private TimeCode(String s){
        setTC(s);
    }

    /**
     * Конструктор получает ТС в виде int 23:59:59:24, только без разделителей: 23595924
     */
    private TimeCode(int i){
        setTC(i);
    }

    TimeCode(TimeCode TC){
        setTC(TC.TCIntStr);
    }

    /**
     * Метод получает ТС в виде int 23:59:59:24, только без разделителей: 23595924
     */
    private void setTC(int i){
        if (i < 0 && i >= 23595924){
            System.out.println("Таймкод должен быть от 0 до 23:59:59:24");
            return;
        }
        TCIntStr = i;
        //Заполняем массив интов
        TCArrayInt[0] = i / 1_000_000;    // часы
        i = i - 1_000_000 * TCArrayInt[0];

        TCArrayInt[1] = i / 10_000;  // минуты
        i = i - 10_000 * TCArrayInt[1];

        TCArrayInt[2] = i / 100;    // секунды
        TCArrayInt[3] = i - 100 * TCArrayInt[2]; // кадры

        if (TCArrayInt [1] > 59 || TCArrayInt [2] > 59 || TCArrayInt[3] > 24){
            System.out.println("Минут и секунд не может быть больше 59, а кадров больше 24. TC = 00:00:00:00.");
            TCArrayInt[0] = 0;
            TCArrayInt[1] = 0;
            TCArrayInt[2] = 0;
            TCArrayInt[3] = 0;
        }

        //Заполняем массив стрингов
        for(int j = 0; j < 4; j++){
            if (TCArrayInt[j] < 10)  TCArrayStr[j] = ( '0' + Integer.toString(TCArrayInt[j]));
            else TCArrayStr[j] = Integer.toString(TCArrayInt[j]);
        }

        //Преобразовываем в int в кадрах
        TCInFrame = 90_000 * TCArrayInt[0] + 1500 * TCArrayInt[1] + 25 * TCArrayInt[2] + TCArrayInt[3];

        // Преобразовываем в сторку формата "23:59:59:24"
        StringBuilder buf = new StringBuilder();
        buf.append(TCArrayStr[0]);
        buf.append(':');
        buf.append(TCArrayStr[1]);
        buf.append(':');
        buf.append(TCArrayStr[2]);
        buf.append(':');
        buf.append(TCArrayStr[3]);
        TCStr = buf.toString();
    }

    /**
     * Метод получает ТС в виде String ТОЛЬКО в таком формате (11 символов с разделителями-двоеточиями):     23:59:59:24
     */
    void setTC(String s1){
        setTC(TCStrToIntStr(s1));
    }

    /**
     * Получаем ТС в виде String с разделителями-двоеточиями     23:59:59:24
     */
    String getTCStr(){
        return TCStr;
    }

    /**
     * Получаем ТС в виде int (HH:MM:SS:FF) без разделителей   23595924
     */
    int getTCIntStr(){
        return TCIntStr;
    }

    /**
     * Получаем ТС в виде одного числа int. Часы, минуты, секунды, кадры пересчитаны в количество кадров. HH*90_000 + MM*1500 + SS*25 + FF
     */
    int getTCInFrame(){
        return TCInFrame;
    }

    /**
     * Метод отображает ТС в виде  23:59:59:24
     */

    @Override
    public String toString(){
        return TCStr;
    }

    void showTC(){
        System.out.println(TCStr);
    }

    TimeCode addTC(TimeCode TC2){
        int temp;
        temp = this.TCInFrame + TC2.TCInFrame;
        if (temp >= MAX_VALUE)
            temp -= MAX_VALUE;
        return new TimeCode(TCInFrameToIntStr(temp));
    }

    TimeCode addTC(String TCstr){
        TimeCode tempTC = new TimeCode(TCstr);
        return addTC(tempTC);
    }

    TimeCode addTC(int TCintstr){
        TimeCode tempTC = new TimeCode(TCintstr);
        return addTC(tempTC);
    }

    void appendTC(TimeCode TC2){
        int temp;
        temp = this.TCInFrame + TC2.TCInFrame;
        if (temp >= MAX_VALUE)
            temp -= MAX_VALUE;
        setTC(TCInFrameToIntStr(temp));
    }

    void appendTC(String TCstr){
        appendTC(TCStrToIntStr(TCstr));
    }

    void appendTC(int TCintstr){
        int temp = this.TCInFrame + TCIntStrToFrame(TCintstr);
        if (temp >= MAX_VALUE)
            temp -= MAX_VALUE;
        setTC(TCInFrameToIntStr(temp));
    }

    /**
     * Метод получает ТС в кадрах и приводит его к виду (int) 23595924
     */

    static int TCInFrameToIntStr(int i){
        int hh, mm, ss, ff;
        hh = i/90_000;
        i = i - 90_000 * hh;
        mm = i/1500;
        i = i - 1500 * mm;
        ss = i/25;
        ff = i - 25 * ss;
        return 1_000_000 * hh + 10_000 * mm + 100 * ss + ff;
    }

    static String TCInFrameToStr(int TC){
        TimeCode.TCInFrameToIntStr(TC);
        return new TimeCode(TimeCode.TCInFrameToIntStr(TC)).getTCStr();
    }

    static int TCStrToFrame(String s){
        char [] array = s.toCharArray();
        char arrayTemp [] = new char[8];


        if(s.length() != 11){
            System.out.println("Таймкод должен быть в формате HH:MM:SS:FF. TC = 00:00:00:00. error 1" + s);
            return 0;
        }

        if((array[2] & array[5] & array[8]) != ':'){
            System.out.println("Таймкод должен быть в формате HH:MM:SS:FF. TC = 00:00:00:00. error 2 " + s);
            return 0;
        }

        for(int i = 0, j = 0; i<11; i++){
            if(array[i] >= '0' & array[i] <= '9'){
                arrayTemp[j++] = array[i];
            } else if(i == 2 | i == 5 | i==8){
                //  continue;
            } else {
                System.out.println("Таймкод должен быть в формате HH:MM:SS:FF. TC = 00:00:00:00.");
                return 0;
            }
        }
        int i = Integer.parseInt(new String(arrayTemp));

        int hh, mm, ss, ff;
        hh = i / 1_000_000;    // часы
        i = i - 1_000_000 * hh;
        mm = i / 10_000;  // минуты
        i = i - 10_000 * mm;
        ss = i / 100;    // секунды
        ff = i - 100 * ss; // кадры
        return 90_000 * hh + 1500 * mm + 25 * ss + ff;
    }

    static int TCIntStrToFrame(int i){
        int hh, mm, ss, ff;
        hh = i / 1_000_000;    // часы
        i = i - 1_000_000 * hh;
        mm = i / 10_000;  // минуты
        i = i - 10_000 * mm;
        ss = i / 100;    // секунды
        ff = i - 100 * ss; // кадры
        return 90_000 * hh + 1500 * mm + 25 * ss + ff;
    }

    static int TCStrToIntStr(String s){
        char [] array = s.toCharArray();
        char arrayTemp [] = new char[8];

        if(s.length() != 11){
            System.out.println("Таймкод должен быть в формате HH:MM:SS:FF. TC = 00:00:00:00.");
            return 0;
        }

        if((array[2] & array[5] & array[8]) != ':'){
            System.out.println("Таймкод должен быть в формате HH:MM:SS:FF. TC = 00:00:00:00.");
            return 0;
        }

        for(int i = 0, j = 0; i<11; i++){
            if(array[i] >= '0' & array[i] <= '9'){
                arrayTemp[j++] = array[i];
            } else if(i == 2 | i == 5 | i==8){
                //  continue;
            } else {
                System.out.println("Таймкод должен быть в формате HH:MM:SS:FF. TC = 00:00:00:00.");
                return 0;
            }
        }
        return Integer.parseInt(new String(arrayTemp));
    }

    boolean checkTC(String tc_in, String tc_out, String tc_dur){   //можно удалять
        boolean trueFalseFlag;
        int in = TCIntStrToFrame(TCStrToIntStr(tc_in));
        int out = TCIntStrToFrame(TCStrToIntStr(tc_out));
        int dur = TCIntStrToFrame(TCStrToIntStr(tc_dur));
        trueFalseFlag = (in == (out - dur));
        if(!trueFalseFlag && (in > out) ){
            out += 2160000; // Переход через полночь
            trueFalseFlag = (in == (out - dur));
        }
        //    System.out.println("IN = " + tc_in + ",  OUT = " + tc_out + ",  DUR = " + tc_dur + " is " + trueFalseFlag);
        return trueFalseFlag;
    }
}



