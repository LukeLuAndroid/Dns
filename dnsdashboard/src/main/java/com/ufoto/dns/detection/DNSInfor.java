package com.ufoto.dns.detection;

public class DNSInfor {
    private long mStartTime;
    private long mEndTime;
    private long mResolutionTime;
    private String mDomain;
    private boolean mHookSuccess;

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long endTime) {
        mEndTime = endTime;
    }

    public long getResolutionTime() {
        return mEndTime - mStartTime;
    }

    private void setResolutionTime(long resolutionTime) {
        mResolutionTime = resolutionTime;
    }

    public String getDomain() {
        return mDomain;
    }

    public void setDomain(String domain) {
        mDomain = domain;
    }

    public boolean isHookSuccess() {
        return mHookSuccess;
    }

    public void setHookSuccess(boolean hookSuccess) {
        mHookSuccess = hookSuccess;
    }
}
