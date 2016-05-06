package com.stanleyidesis.quotograph.billing.util;

public class IabLic {

    static private String stringTransform(String s, int i) {
        char[] chars = s.toCharArray();
        for(int j = 0; j < chars.length; j++) {
            chars[j] = (char) (chars[j] ^ i);
        }
        return String.valueOf(chars);
    }

    public static String retrieveLic() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgkZshH1mt/RajVPCZQQwKw01OKNRM74oMmzb80Z/d2GRZhgTLZZITQeDNr/KVJTZ1vchCbvknC+ykuywNOpJ7LxEshkIfbmMwmSPPgFEXoG0Sw2IbsWemipRRgT8p3p6BIRiGsg214ShAYUqa0fsmsWu/7RzDdDaYEn2YPaB6Jr8Xb2MBcMSa9U1OfEq8aOXlRVJV0AgbL7py8jozL0+KASdlwzRWmmQ6TpRBNRrvuaAME/or10DB52qWmKnURS9nSZs6wTdyva58w5k8mtc6EGGDWFwnRvKz7/KbhCYOMNheKoh80AGRtnvNBPOZa82A0RL3AhvrA0qejJ5ZDJ3UQIDAQAB";
    }
}
