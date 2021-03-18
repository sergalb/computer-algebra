import kotlin.math.*
import kotlin.random.Random
import kotlin.random.Random.Default.nextLong
import kotlin.system.measureTimeMillis


fun generatePrimes(): Sequence<Long> {
    var i = 2L
    return sequence {
        generateSequence { i++ }
            .filter { n ->
                when {
                    n == 1L -> false
                    n < 4 -> true
                    n.rem(2) == 0L -> false
                    n < 9 -> true
                    n.rem(3) == 0L -> false
                    else -> {
                        val r = floor(sqrt(n.toDouble()))
                        var f = 5
                        while (f <= r) {
                            if (n.rem(f) == 0L || n.rem(f + 2) == 0L) return@filter false
                            f += 6
                        }
                        true
                    }
                }
            }
            .forEach { yield(it) }
    }
}

fun generateBSmooth(
    h: Int,
    n: Long,
    primes: List<Long>
): Triple<MutableList<Long>, Array<Array<Int>>, Array<Array<Long>>> {
    val bSmooth: MutableList<Long> = ArrayList(h + 1)
    val alphas = Array(h + 1) { Array(primes.size) { 0 } }
    val epsilons = Array(h + 1) { Array(primes.size) { 0L } }
    var countBSmooth = 0
    while (countBSmooth < h + 1) {
        val candidate = nextLong(sqrt(n.toDouble()).toLong(), n)
        var a = (candidate * candidate) % n
        val alpha = Array(h) { 0 }
        for (i in primes.indices) {
            val prime = primes[i]
            var power = 0
            while (a % prime == 0L) {
                power++
                a /= prime
            }
            alpha[i] = power
            epsilons[countBSmooth][i] = (power % 2).toLong()
        }
        if (a == 1L) {
            alphas[countBSmooth] = alpha
            countBSmooth++
            bSmooth.add(candidate)
        }

    }
    return Triple(bSmooth, alphas, epsilons)
}

fun L(n: Double) = sqrt(exp(sqrt(ln(n) * ln(ln(n)))))

private fun gcd(a: Long, b: Long): Long {
    if (a == 0L) return b
    return gcd(b % a, a)
}

fun factorize(n: Long) {
    val m = L(n.toDouble())
    val primes = generatePrimes().take(m.toInt() / 2).filter { it <= m.toInt() }.toList()
    val h = primes.size
    var isFactorized = false
    for (attempt in 0..30) {
        val t = generateBSmooth(h, n, primes)
        val bSmooth = t.first
        val alphas = t.second
        val epsilons = t.third
        val matrix = SparseMatrix(2 shl 20, epsilons)
        matrix.inPlaceTranspose()
        val solutionAttempt = matrix.conjugateGradientMethod()
        if (solutionAttempt.isPresent) {
            val x = solutionAttempt.get()
            var left = (1L).toBigInteger()
            val gamma = Array(primes.size) { 0 }
            for (i in x.indices) {
                if (x[i] != 0L) {
                    left = left * bSmooth[i].toBigInteger() % n.toBigInteger()
                    for (j in primes.indices) {
                        gamma[j] += alphas[i][j]
                    }
                }
            }
            var right = (1L).toBigInteger()
            for (i in primes.indices) {
                gamma[i] /= 2
                right = right * primes[i].toDouble().pow(gamma[i]).toLong()
                    .toBigInteger() % n.toBigInteger()
            }
            val sumGcd = gcd((left + right).toLong(), n)
            val subGcd = gcd((left - right).toLong(), n)
            if (left != right && left != n.toBigInteger() - right && (abs(sumGcd) != 1L || abs(subGcd) != 1L)) {
                println("s, t from Dixon algorithm: $left, $right")
                println("factor: ${left + right}, gcd: $sumGcd")
                println("factor: ${left - right}, gcd: $subGcd")
                isFactorized = true
                break
            }
        }
            println("attempt $attempt for factorizing is failed")

    }
    if (!isFactorized) {
        println("couldn't factorize, may be input is prime?")
    }


}
fun main() {
    val n = 37 * 59 * 61 * 97L
//    val n = 89755L

//    val n = 1125899839733757L
    factorize(n)
}
