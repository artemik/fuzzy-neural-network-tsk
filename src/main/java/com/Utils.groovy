package com

import groovy.transform.CompileStatic

/**
 * Created by Artem on 24.04.2017.
 */
@CompileStatic
class Utils {
    static double sum(int bound, OperationBody body) {
        (0..<bound).inject(0.0) { total, i -> total + body.calculate((int) i) }
    }

    static double product(int bound, OperationBody body) {
        (0..<bound).inject(1.0) { total, i -> total * body.calculate((int) i) }
    }

    static void upto(int to, OperationBodyWithoutResult body) {
        (0..<to).each { i -> body.call((int) i) }
    }

    static <T> List<T> collectUpto(int to, OperationBodyWithResult<T> body) {
        return collectUpto(0, to, body)
    }

    static <T> List<T> collectUpto(int from, int to, OperationBodyWithResult<T> body) {
        return (from..<to).collect { i -> body.call((int) i) }
    }

    interface OperationBody {
        double calculate(int i)
    }

    interface OperationBodyWithoutResult {
        void call(int i)
    }

    interface OperationBodyWithResult<T> {
        T call(int i)
    }
}
