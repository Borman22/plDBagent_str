package tv.sonce.pldbagent.controller;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Этот класс занимается валидацией и парсингом файлов
 */

class FileParser {

    private static final Logger LOGGER = Logger.getLogger(FileParser.class);

    List<Event> parse(File file) {
        // 1. Валидация файла
        if(!file.getName().endsWith(".xml"))
            return null;

        // 2. Превращение файла в DOM и еще одна валидация
        DocumentBuilder db;
        Document doc;
        NodeList nodeListSchedule;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder(); // создали конкретного строителя документа
            doc = db.parse(file); // строитель построил документ
            nodeListSchedule = doc.getElementsByTagName("event"); // получаем все Event из родительского Schedule
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Не получилось распарсить XML ", e);
            return null;
        }
        if(!doc.getDocumentElement().getNodeName().equals("CobaltAsRun"))
            return null; // наш документн - это xml, который создан не с помощью Cobalt

        // 3. Получение количества Events и парсинг каждого Event в отдельности
        String tempDateStr, tempTimeStr, tempTcInStr, tempTcOutStr, tempDurationStr, tempAsset_idStr, tempEventNameStr, tempFormatStr;

        List<Event> table = new ArrayList<>();


        for (int i = 0; i < nodeListSchedule.getLength(); i++) {
            Element curentElement = (Element) nodeListSchedule.item(i);

            tempDateStr = curentElement.getElementsByTagName("date").item(0).getChildNodes().item(0).getNodeValue().trim();
            tempTimeStr = curentElement.getElementsByTagName("time").item(0).getChildNodes().item(0).getNodeValue().trim();
            tempDurationStr = curentElement.getElementsByTagName("duration").item(0).getChildNodes().item(0).getNodeValue().trim();
            tempAsset_idStr = curentElement.getElementsByTagName("asset_id").item(0).getChildNodes().item(0).getNodeValue().trim();
            tempEventNameStr = curentElement.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue().trim().replace('\'', ' ');
            tempFormatStr = curentElement.getElementsByTagName("format").item(0).getChildNodes().item(0).getNodeValue().trim();

            if(curentElement.getElementsByTagName("tc_in").item(0) == null) tempTcInStr = null;
            else {
                if(curentElement.getElementsByTagName("tc_in").item(0).getChildNodes().item(0) == null)
                    tempTcInStr = null;
                else
                    tempTcInStr = curentElement.getElementsByTagName("tc_in").item(0).getChildNodes().item(0).getNodeValue().trim().substring(0,11);
            }


            if(curentElement.getElementsByTagName("tc_out").item(0) == null) tempTcOutStr = null;
            else {
                if(curentElement.getElementsByTagName("tc_out").item(0).getChildNodes().item(0) == null)
                    tempTcOutStr = null;
                else
                    tempTcOutStr = curentElement.getElementsByTagName("tc_out").item(0).getChildNodes().item(0).getNodeValue().trim().substring(0,11);
            }


            // 4. Приведение значений каждого поля к нужному виду
            int tempDate, tempTime, tempTcIn, tempTcOut, tempDuration, tempAsset_id;
            String tempEventName, tempFormat [];

            StringBuilder sb = new StringBuilder();
            sb.append(tempDateStr.substring(0,4)).append(tempDateStr.substring(5,7)).append(tempDateStr.substring(8));
            tempDate = Integer.parseInt(sb.toString());     // 2017-06-20

//            Date date = Date.valueOf(tempDateStr);
//            System.out.println("Дата = " + date);

            tempTime = TimeCode.TCStrToIntStr(tempTimeStr);
            tempTcIn = (tempTcInStr == null) ? -1 :  TimeCode.TCStrToIntStr(tempTcInStr);
            tempTcOut = (tempTcOutStr == null) ? -1 :  TimeCode.TCStrToIntStr(tempTcOutStr);
            tempDuration = TimeCode.TCStrToIntStr(tempDurationStr);
            tempAsset_id = Integer.parseInt(tempAsset_idStr);
            tempEventName = tempEventNameStr;
            tempFormat = tempFormatStr.split(",");
            for (int j = 0; j < tempFormat.length; j++) {
                tempFormat[j] = tempFormat[j].trim();
            }

            // 5. Добавление в List<Events>

            table.add(new Event(tempDate, tempTime, tempDuration, tempAsset_id, tempEventName, tempFormat, tempTcIn, tempTcOut));
        }
        return table;
    }

    class Event {
        int date;
        int time;
        int duration;
        int asset_id;
        String eventName;
        String [] format;
        int tcIn;
        int tcOut;

        Event(int date, int time, int duration, int asset_id, String eventName, String[] format, int tcIn, int tcOut) {
            this.date = date;
            this.time = time;
            this.duration = duration;
            this.asset_id = asset_id;
            this.eventName = eventName;
            this.format = format;
            this.tcIn = tcIn;
            this.tcOut = tcOut;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "date=" + date +
                    ", time=" + time +
                    ", duration=" + duration +
                    ", asset_id=" + asset_id +
                    ", eventName='" + eventName + '\'' +
                    ", format=" + Arrays.toString(format) +
                    ", tcIn=" + tcIn +
                    ", tcOut=" + tcOut +
                    '}';
        }
    }

}
