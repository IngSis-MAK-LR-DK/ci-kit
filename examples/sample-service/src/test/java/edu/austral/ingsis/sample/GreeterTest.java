package edu.austral.ingsis.sample;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GreeterTest {
    @Test
    void greetsByName() {
        assertEquals("Hola, Ana!", new Greeter().greet("Ana"));
    }
}
