package edu.austral.ingsis.sample;

public final class Greeter {
    private static final String PREFIX = "Hola, ";

    public String greet(String name) {
        return PREFIX + name + "!";
    }
}
