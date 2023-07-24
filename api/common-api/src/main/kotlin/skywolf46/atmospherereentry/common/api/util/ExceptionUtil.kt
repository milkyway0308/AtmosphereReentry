package skywolf46.atmospherereentry.common.api.util

fun Throwable.waitStackTrace(){
    printStackTrace()
    System.err.flush()
    Thread.sleep(10L)
}

fun printError(string: String) {
    System.err.println(string)
    System.err.flush()
    Thread.sleep(10L)
}