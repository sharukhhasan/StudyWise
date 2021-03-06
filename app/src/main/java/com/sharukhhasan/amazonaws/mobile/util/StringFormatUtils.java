package com.sharukhhasan.amazonaws.mobile.util;

/**
 * Created by Sharukh on 2/13/16.
 */
public final class StringFormatUtils
{
    private StringFormatUtils() {}

    public static String getBytesString(final long bytes, final boolean higherPrecision)
    {
        final String[] quantifiers = new String[] {
                "KB", "MB", "GB", "TB"
        };
        double size = bytes;
        for(int i = 0;; i++)
        {
            if(i >= quantifiers.length)
            {
                return "\u221E";
            }
            size /= 1024;
            if(size < 512)
            {
                if(higherPrecision)
                {
                    return String.format("%.2f %s", size, quantifiers[i]);
                }
                else
                {
                    return String.format("%d %s", Math.round(size), quantifiers[i]);
                }
            }
        }
    }
}
