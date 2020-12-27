package ddwu.moblie.finalproject.ma01_20180999;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class KopisXmlParser {

    private enum TagType { NONE, TITLE, VENUE, PERIOD }

    private final static String ITEM_TAG = "item";
    private final static String TITLE_TAG = "title";
    private final static String VENUE_TAG = "venue";
    private final static String PERIOD_TAG = "period";

    private XmlPullParser parser;

    public KopisXmlParser() {
        //파서 준비
        XmlPullParserFactory factory = null;

        //파서 생성
        try {
            factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Performance> parse(String xml) {
        ArrayList<Performance> resultList = new ArrayList();
        Performance performance = null;
        TagType tagType = TagType.NONE;

        try {
            // 파싱 대상 지정
            parser.setInput(new StringReader(xml));

            // 태그 유형 구분 변수 준비
            int eventType = parser.getEventType();

            // parsing 수행 - for 문 또는 while 문으로 구성
//            for (int eventType = parser.getEventType();
//                     eventType != XmlPullParser.END_DOCUMENT;
//                     eventType = parser.next())
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if (tag.equals(ITEM_TAG)) {
                            performance = new Performance();
                        } else if (tag.equals(TITLE_TAG)) {
                            tagType = TagType.TITLE;
                        } else if (tag.equals(VENUE_TAG)) {
                            tagType = TagType.VENUE;
                        } else if (tag.equals(PERIOD_TAG)) {
                            tagType = TagType.PERIOD;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals(ITEM_TAG)) {
                            resultList.add(performance);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        switch (tagType) {
                            case TITLE:
                                performance.setTitle((parser.getText()));
                                break;
                            case VENUE:
                                performance.setVenue((parser.getText()));
                                break;
                            case PERIOD:
                                performance.setPeriod((parser.getText()));
                                break;
                        }
                        tagType = TagType.NONE;
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
}
