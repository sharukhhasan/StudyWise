package com.sharukhhasan.studywise.constants;

/**
 * Created by Sharukh on 2/15/16.
 */
public class AppConstants
{
    public enum SharedPreferenceKeys {
        USER_NAME("userName"),
        USER_EMAIL("userEmail"),
        USER_IMAGE_URL("userImageUrl");


        private String value;

        SharedPreferenceKeys(String value) {
            this.value = value;
        }

        public String getKey() {
            return value;
        }
    }
}
