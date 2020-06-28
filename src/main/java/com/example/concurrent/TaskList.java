package com.example.concurrent;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class TaskList {

    public static IntStream tasks = IntStream.range(1, 15);

    public static Supplier<IntStream> taskStreamSupplier
            = () -> IntStream.range(1, 15);
}
