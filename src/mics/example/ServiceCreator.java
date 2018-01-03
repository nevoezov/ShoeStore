package mics.example;

import mics.MicroService;

public interface ServiceCreator {
    MicroService create(String name, String[] args);
}
