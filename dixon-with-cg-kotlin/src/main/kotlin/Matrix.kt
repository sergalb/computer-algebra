import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random.Default.nextLong


class Matrix(private val base: Long, private var data: Array<Array<Long>>) {
    var m: Int = data.size
    var n: Int = data[0].size

    constructor(base: Long, m: Int, n: Int) : this(base, Array(m) { Array(n) { 0L } })

    private fun triangulate() {
        sortByRows()
        var curRow = 0
        for (i in data[0].indices) {
            if (data[curRow][i] == 0L) {
                continue
            }
            for (row in curRow + 1 until m) {
                if (data[row][i] != 0L) {
                    subtractRows(row, curRow, data[row][i].toDouble() / data[curRow][i].toDouble())
                }
            }
            sortByRows()
            curRow += 1
            if (curRow == m) break
        }
//        println(this)
    }

    operator fun get(i: Int, j: Int): Long {
        return data[i][j]
    }

    private fun subtractRows(subtracted: Int, subtracter: Int, coefficient: Double) {
        for (i in data[subtracted].indices) {
            data[subtracted][i] =
                (((data[subtracted][i].toDouble() - data[subtracter][i].toDouble() * coefficient).toLong()) % base + base) % base
        }
    }

    operator fun plus(other: Matrix): Matrix {
        if (other.m != m || other.n != this.n) {
            throw IllegalArgumentException("couldn't add matrix with shape ${other.m} x ${other.n} to matrix with shape $m x $n")
        }
        val res = Array(m) { Array(n) { 0L } }
        for (i in 0 until m) {
            for (j in 0 until n) {
                res[i][j] = (data[i][j] + other.data[i][j]) % base
            }
        }
        return Matrix(base, res)
    }

    operator fun times(other: Matrix): Matrix {
        if (n != other.m) {
            throw IllegalArgumentException("couldn't multiply matrix with shape $m x $n on matrix with shape ${other.m} x ${other.n}")
        }
        val res = Array(m) { Array(other.n) { 0L } }
        for (i in 0 until m) {
            for (j in 0 until other.n) {
                for (k in 0 until n) {
                    res[i][j] = (res[i][j] + data[i][k] * other.data[k][j] % base) % base
                }
            }
        }
        return Matrix(base, res)
    }

    operator fun unaryMinus(): Matrix {
        val res = Array(m) { Array(n) { 0L } }
        for (i in 0 until m) {
            for (j in 0 until n)
                res[i][j] = (-data[i][j] + base) % base
        }
        return Matrix(base, res)
    }

    operator fun minus(other: Matrix): Matrix {
        return this + (-other)
    }

    fun inPlaceTranspose() {
        val transposed = transpose()
        data = transposed.data
        m = transposed.m
        n = transposed.n
    }

    private fun sortByRows() {
        data.sortWith { a, b ->
            var isFirstNonZero = true
            for (i in a.indices) {
                if (a[i] - b[i] != 0L) {
                    return@sortWith if (isFirstNonZero) {
                        (b[i] - a[i]).toInt()
                    } else {
                        (a[i] - b[i]).toInt()
                    }
                }
                if (a[i] == 1L) {
                    isFirstNonZero = false
                }
            }
            return@sortWith 0
        }
    }

    fun transpose(): Matrix {
        val newData = Array(n) { Array(m) { 0L } }
        for (i in data.indices) {
            for (j in data[i].indices) {
                newData[j][i] = data[i][j]
            }
        }
        return Matrix(base, newData)
    }

    private fun isZeroRow(ind: Int) = data[ind].all { it == 0L }

    private fun isZeroColumn(ind: Int): Boolean {
        for (i in data.indices) {
            if (data[i][ind] != 0L) return false
        }
        return true
    }

    fun rang(): Int {
        var r = m
        for (i in data.indices) {
            if (isZeroRow(i)) r--
        }
        return r
    }

    private fun setSingleVar(x: MutableMap<Int, Long>) {
        for (i in 0 until m) {
            var countVars = 0
            var ind = -1
            var rightSide = 0L
            for (j in 0 until n) {

                if (data[i][j] != 0L) {
                    if (x.containsKey(j)) {
                        rightSide -= x[j]!!
                    } else {
                        countVars++
                        ind = j
                    }
                }
                if (countVars > 2) break
            }
            if (countVars == 1) {
                x[ind] = (rightSide % base + base) % base
            }
        }
    }

    fun solveHomogeneousSLE(): Optional<Array<Long>> {
        triangulate()
        var free = n - rang()
        val x: MutableMap<Int, Long> = HashMap(n)
        var hasZero = false
        for (i in 0 until n) {
            if (isZeroColumn(i)) {
                x[i] = 1
                hasZero = true
            }
        }
        if (hasZero) return (Optional.of(Array(n) { x.getOrDefault(it, 0) }))
        setSingleVar(x)
        for (i in n - 1 downTo 0) {
            if (i in x) continue
            if (free > 0) {
                x[i] = 1
                free--
            }
            setSingleVar(x)
        }

        return if (x.size == n) {
            Optional.of(Array(n) { x.getOrDefault(it, 0) })
        } else {
            Optional.empty()
        }
    }

    fun scalarMultiply(c: Long): Matrix {
        val res = Array(m) { Array(n) { 0L } }
        for (i in 0 until m) {
            for (j in 0 until n) {
                res[i][j] = data[i][j] * c % base
            }
        }
        return Matrix(base, res)
    }

    private fun generateRandomDiagonalMatrix(h: Int): Matrix {
        val res = Array(h) { Array(h) { 0L } }
        for (i in 0 until h) {
            val d = nextLong(1, base)
            res[i][i] = d * d % base
        }
        return Matrix(base, res)
    }

    fun conjugateGradientMethod(): Optional<Array<Long>> {
        var x = Matrix(base, Array(n) { arrayOf(nextLong(0,2)) })
        val squaredD = generateRandomDiagonalMatrix(m)
        val matrixA = this.transpose() * squaredD * this
        var r = Matrix(base, n, 1) - matrixA * x
        var z = r
        var a: Long
        var beta: Long
        var rPrev: Matrix
        for (i in 0..n) {
            a = ((r.transpose() * r).data[0][0]) / ((matrixA * z).transpose() * z).data[0][0]
            x += z.scalarMultiply(a)
            rPrev = r
            r -= (matrixA * z).scalarMultiply(a)
            beta = ((r.transpose() * r).data[0][0]) / (rPrev.transpose() * rPrev).data[0][0]
            z = r + z.scalarMultiply(beta)
        }

        return Optional.of(Array(n) {x.data[it][0]})
    }

    override fun toString(): String {
        return "Matrix(base=$base, " +
                "data= ${System.lineSeparator()}" +
                "${data.joinToString(separator = System.lineSeparator()) { it.contentToString() }})"
    }


}