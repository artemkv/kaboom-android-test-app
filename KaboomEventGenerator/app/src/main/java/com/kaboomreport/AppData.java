package com.kaboomreport;

import java.util.UUID;

final class AppData {
    private final UUID userId;

    public AppData() {
        userId = UUID.randomUUID();
    }

    public AppData(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
