package com.stanleyidesis.quotograph.billing.util;

public class IabLic {

    static private final String lic1 = "鈏鈋鈋鈀鈋鈨鈃鈌鈀鈥鈩鈳鈪鈩鈫";
    static private final String lic2 = "鈅鉻鈵鉲鈀鈃鈓鈇鈄鈃鈃鈍鈁鈃鈓鉺鈃鈏";
    static private final String lic3 = "鈋鈋鈀鈁鈥鈉鈁鈃鈓鈇鈃鈥鈩鈘鈱鈪鈊鉳鈯鈶鉭鈐鈣";
    static private final String lic4 = "鈨鈔鈒鈁鈘鈓鈓鈵鈉鈵鉲鉳鈍鈉鈌鈐鈏鉵鉶鈭鈏鈯鈸鈠鉺";
    static private final String lic5 = "鉲鈘鉭鈦鉰鈅鈐鈘鈪鈥鈖鈎鈘鈘鈋鈖鈓鈧鈆鈌鈰";
    static private final String lic6 = "鉭鈉鈔鈈鈖鈘鉳鈴鈡鈪鈁鈠鈴鈩鈬鈁鉩鈻";
    static private final String lic7 = "鈩鈷鈻鈵鈌鈍鈲鈈鉵鈎鈺鈇鈱鈪鈩鈋鈤鈠鈯鈏鈵鈯鈑鈒鈒鈥";
    static private final String lic8 = "鈄鈇鈚鈭鈅鉲鈑鈵鉰鈋鈠鈱鈕鈧鈯鈫鈲鈐鈐鈥鈖鉺鈲";
    static private final String lic9 = "鉱鈲鉴鈀鈋鈐鈫鈅鈱鈥鉰鉳鉶鈑鈪鈃鈛鈗鈳鈣";
    static private final String lic10 = "鉲鈤鈱鈯鈱鈕鈷鉭鉵鈐鈸鈆鈦鈆鈣鈛鈇鈬";
    static private final String lic11 = "鉰鈛鈒鈣鈀鉴鈈鈰鉺鈚鈠鉰鈏鈀鈡";
    static private final String lic12 = "鈏鈑鈣鉻鈗鉳鈍鈤鈇鈳鉺鈣鈍鈚鈮鈐鈔鈈鈔鉲";
    static private final String lic13 = "鈃鈥鈠鈎鉵鈲鈻鉺鈨鈭鈸鈎鉲鉩鈉鈃鈑";
    static private final String lic14 = "鈦鈮鈵鈸鈐鈕鈯鈯鈓鉴鈖鈲鈐鈀鈌鈐鈰";
    static private final String lic15 = "鈴鈷鈣鈃鈏鈇鉭鈭鈰鉳鉲鈆鈀鉷鉰鈳鈕鈯鈉鈬鈗鈐";
    static private final String lic16 = "鈑鉻鈬鈑鈘鈱鉴鈵鈖鈦鈻鈴鈣鉷鉺鈵鉷鈩";
    static private final String lic17 = "鉺鈯鈶鈡鉴鈇鈅鈅鈆鈕鈄鈵鈬鈐鈴鈉";
    static private final String lic18 = "鈸鉵鉭鈉鈠鈪鈁鈛鈍鈏鈌鈪鈧鈉鈭鈪鉺鉲";
    static private final String lic19 = "鈃鈅鈐鈶鈬鈴鈌鈀鈒鈍鈘鈣鉺鉰鈃鉲鈐鈎鉱鈃鈪鈴";
    static private final String lic20 = "鈰鈃鉲鈳鈧鈨鈈鉷鈘鈆鈈鉱鈗鈓鈋鈆鈃鈓鈃鈀";

    static private String stringTransform(String s, int i) {
        char[] chars = s.toCharArray();
        for(int j = 0; j < chars.length; j++) {
            chars[j] = (char) (chars[j] ^ i);
        }
        return String.valueOf(chars);
    }

    static private String licCombiner() {
        return lic1 + lic2 + lic3 + lic4 + lic5 + lic6 + lic7 + lic8 + lic9 + lic10 + lic11 + lic12
                + lic13 + lic14 + lic15 + lic16 + lic17 + lic18 + lic19 + lic20;
    }

    public static String retrieveLic() {
        return stringTransform(licCombiner(), 0x942);
    }
}
