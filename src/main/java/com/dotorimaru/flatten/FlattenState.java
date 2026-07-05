package com.dotorimaru.flatten;

/** 플레이어별 평탄화 상태 (메모리 전용). */
public final class FlattenState {

    private boolean enabled;
    private int y;
    private int radius;

    // 마지막으로 평탄화를 수행한 블록 좌표 (같은 블록 내 재처리 방지)
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;

    public FlattenState(int y, int radius) {
        this.y = y;
        this.radius = radius;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean b) { this.enabled = b; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getRadius() { return radius; }
    public void setRadius(int r) { this.radius = r; }

    /** 지정 좌표가 마지막 처리 위치와 다른 블록인지 여부. */
    public boolean moved(int x, int z) {
        return x != lastX || z != lastZ;
    }

    public void mark(int x, int z) {
        this.lastX = x;
        this.lastZ = z;
    }

    /** 다음 이동 시 무조건 1회 처리되도록 마지막 위치 초기화. */
    public void resetMark() {
        this.lastX = Integer.MIN_VALUE;
        this.lastZ = Integer.MIN_VALUE;
    }
}
