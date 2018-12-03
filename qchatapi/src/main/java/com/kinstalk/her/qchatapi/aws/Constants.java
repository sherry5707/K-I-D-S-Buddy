package com.kinstalk.her.qchatapi.aws;

/**
 * Created by wangzhipeng on 2018/4/6.
 */

public class Constants {
    /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
    public static final String COGNITO_POOL_ID = "cn-north-1:2a048c25-e1f3-4d02-9bef-fb121aafc50e";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "cn-north-1";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_QWX = "qwx";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "cn-north-1";
}
