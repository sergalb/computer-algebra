import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random.Default.nextLong


class SparseMatrix(val base: Long, var m: Int, var n: Int) {
    var data: MutableMap<Int, MutableMap<Int, Long>> = HashMap()


    constructor(base: Long, fullData: Array<Array<Long>>) : this(base, fullData.size, fullData[0].size) {
        for (i in 0 until m) {
            for (j in 0 until n) {
                if (fullData[i][j] != 0L) {
                    set(i,j, fullData[i][j])
                }
            }
        }
    }

    constructor(other: SparseMatrix) : this(other.base, other.m, other.n) {
        for ((row, map) in other.data) {
            data[row] = HashMap()
            data[row]!!.putAll(map)
        }
    }

    operator fun plus(other: SparseMatrix): SparseMatrix {
        if (other.m != m || other.n != this.n) {
            throw IllegalArgumentException("couldn't add SparseMatrix with shape ${other.m} x ${other.n} to SparseMatrix with shape $m x $n")
        }
        val res = SparseMatrix(this)
        for ((row, map) in other.data) {
            for ((column, value) in map) {
                if (res.data.containsKey(row)) {
                    res.data[row]!!.merge(column, value) { old, new ->
                        (old + new) % base
                    }
                } else {
                    res.data[row] = mutableMapOf(Pair(column, value))
                }
            }
        }
        return res
    }

    operator fun set(i: Int, j: Int, value: Long) {
        if (data.containsKey(i)) {
            data[i]!![j] = value
        } else {
            data[i] = mutableMapOf((j to value))
        }
    }

    operator fun times(other: SparseMatrix): SparseMatrix {
        if (n != other.m) {
            throw IllegalArgumentException("couldn't multiply SparseMatrix with shape ${m} x ${n} on SparseMatrix with shape ${other.m} x ${other.n}")
        }
        val res = SparseMatrix(base, m, other.n)

        for ((row, map) in data) {
            for ((leftColumn, leftValue) in map) {
                other.data[leftColumn]?.forEach { (rightColumn, rightValue) ->
                    val mult = leftValue * rightValue % base
                    res[row, rightColumn] = (res[row, rightColumn] + mult + base) % base
                }
            }
        }
        return res
    }

    operator fun unaryMinus(): SparseMatrix {
        return scalarMultiply(-1)
    }

    operator fun minus(other: SparseMatrix): SparseMatrix {
        return this + (-other)
    }

    fun inPlaceTranspose() {
        val transposed = transpose()
        data = transposed.data
        m = transposed.m
        n = transposed.n
    }

    fun transpose(): SparseMatrix {
        val res = SparseMatrix(base, n, m)
        val newData = res.data
        for ((row, map) in data) {
            for ((column, value) in map) {
                if (newData.containsKey(column)) {
                    newData[column]!![row] = value
                } else {
                    newData[column] = mutableMapOf((row to value))
                }
            }
        }
        return res
    }

    fun scalarMultiply(c: Long): SparseMatrix {
        val res = SparseMatrix(this)
        res.data.values.forEach { it.values.map { inner -> (inner * c + base) % base } }
        return res
    }

    private fun generateRandomDiagonalSparseMatrix(h: Int): SparseMatrix {
        val res = Array(h) { Array(h) { 0L } }
        for (i in 0 until h) {
            val d = nextLong(1, base)
            res[i][i] = d * d % base
        }
        return SparseMatrix(base, res)
    }

    operator fun get(i: Int, j: Int): Long {
        return data[i]?.get(j) ?: 0L
    }

    fun conjugateGradientMethod(): Optional<Array<Long>> {
        var x = SparseMatrix(base, Array(n) { arrayOf(nextLong(0, 2)) })
        val squaredD = generateRandomDiagonalSparseMatrix(m)
        val SparseMatrixA = this.transpose() * (squaredD) * this
        var r = SparseMatrix(base, n, 1) - SparseMatrixA * x
        var z = r
        var a: Long
        var beta: Long
        var rPrev: SparseMatrix
        for (i in 0..n) {
            a = ((r.transpose() * r)[0, 0]) / ((SparseMatrixA * z).transpose() * z)[0, 0]
            x += z.scalarMultiply(a)
            rPrev = r
            r -= (SparseMatrixA * z).scalarMultiply(a)
            beta = ((r.transpose() * r)[0, 0]) / (rPrev.transpose() * rPrev)[0, 0]
            z = r + z.scalarMultiply(beta)
        }

        return Optional.of(Array(n) { x.data[it]?.get(0) ?: 0L })
    }

    override fun toString(): String {
        return "SparseMatrix(base=$base, m = $m, n = $n " +
                "data= ${System.lineSeparator()}" +
                (0 until m).joinToString(separator = System.lineSeparator())
                {"$it: ${data[it]?.toString() ?: "zeroes"}"  }
    }
}