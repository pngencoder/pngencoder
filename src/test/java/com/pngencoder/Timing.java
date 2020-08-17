package com.pngencoder;

class Timing {
    private static long previously = 0;
    static void message(String message) {
        long now = System.currentTimeMillis();
        long delta = previously > 0 ? now - previously : 0;
        previously = now;
        System.out.println("[" + delta + "] " + message);
    }
}
