package com.firzzle.common.constant;

public enum CubeOneItem {
    ADDRESS("ADDRESS"), /*주소*/
    CARD("CARD"), /*카드번호*/
    CARD6("CARD6"), /*카드번호*/
    DEPOSITF("DEPOSITF"), /*계좌번호*/
    MAIL("MAIL"), /*이메일*/
    NAME("NAME"), /*이름*/
    PHONE("PHONE"), /*전화번호*/
    PWD("PWD"), /*비밀번호*/
    SSNF("SSNF"), /*주민번호, 사업자번호*/
    TRANDATA("TRANDATA"), /*전문 데이터*/
    CONTENT("CONTENT"), /*전문 데이터*/
    LOCATE("LOCATE") /*위치 정보*/;

    private String cubeoneItem;

    CubeOneItem(String cubeoneItem) {
        this.cubeoneItem = cubeoneItem;
    }

    public String getCubeoneItem() {
        return cubeoneItem;
    }

    public static CubeOneItem getCubeOneItem(String target) {
        if (target.contains("email")) {
            return MAIL;
        }
        if (target.contains("phone") || target.contains("mobile") || target.contains("destine")  || target.contains("sms")) {
            return PHONE;
        }
        if (target.contains("addr")) {
            return ADDRESS;
        }
        if (target.contains("bank_number")) {
            return DEPOSITF;
        }
        if (target.contains("rrn") || target.contains("resno")) {
            return SSNF;
        }
        if (target.contains("pwd")) {
            return PWD;
        }
        throw new IllegalArgumentException("CubeOne 암복호화 오류 : item값 없음");
    }
}

