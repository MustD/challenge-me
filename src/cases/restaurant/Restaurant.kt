package cases.restaurant

import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid


// single restaurant
// create tables / have set of tables with capacity
// user should be able to book table for period of time in future
// user should be able to cancel booking


@JvmInline
value class RtTableId(private val id: Uuid = Uuid.generateV7())
data class RtTable(val id: RtTableId, val capacity: Int)

interface RtWithTable {
    fun addTable(tableCapacity: Int): RtTable
    fun tableList(): List<RtTable>
}

@JvmInline
value class RtBookingId(private val id: Uuid = Uuid.generateV7())
data class RtBooking(
    val id: RtBookingId = RtBookingId(),
    val capacity: Int,
    val name: String,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
)

interface RtBookable {
    fun book(request: RtBooking): RtBookingId?
    fun cancel(id: RtBookingId)
}

class RtService : RtWithTable, RtBookable {

    private val tables = ConcurrentHashMap<RtTableId, RtTable>()

    override fun addTable(tableCapacity: Int): RtTable {
        val newTable = RtTable(RtTableId(), tableCapacity)
        return tables.computeIfAbsent(newTable.id) { newTable }
    }

    override fun tableList(): List<RtTable> {
        return tables.asSequence().map { it.value }.toList()
    }

    private val bookings = ConcurrentHashMap<RtTableId, TreeMap<OffsetDateTime, RtBooking>>()

    override fun book(request: RtBooking): RtBookingId? {
        val available = tables.keys.asSequence().map { tableId ->
            tableId to bookings.computeIfAbsent(tableId) { TreeMap() }
        }.filter { (tableId, tableBookings) ->
            val table = tables[tableId] ?: throw IllegalStateException("Unexpected: Table not found")

            if (table.capacity < request.capacity) return@filter false
            if (tableBookings.isEmpty()) return@filter true

            val prev = tableBookings.floorEntry(request.startTime)
            val isNotIntersectWithPrev = prev != null && prev.value.endTime < request.startTime

            val next = tableBookings.ceilingEntry(request.startTime)
            val isNotIntersectWithNext = next != null && next.value.startTime > request.endTime
            if (isNotIntersectWithPrev && isNotIntersectWithNext) return@filter true
            false
        }.toList()

        if (available.isEmpty()) return null //no free table found

        //sorted by capacity
        val availableTables = available.map { (availableId, _) ->
            tables[availableId]?.let {
                it.id to it.capacity
            } ?: throw IllegalStateException("Unexpected: Available table not found")
        }.sortedBy { (_, capacity) -> capacity }

        //book first available table
        availableTables.first().let { (tableId, _) ->
            bookings.compute(tableId) { _, bookings ->
                (bookings ?: TreeMap()).apply { put(request.startTime, request) }
            }
        }


        return RtBookingId()
    }

    //O(n) todo: think about
    override fun cancel(id: RtBookingId) {
        val (tableId, key) = bookings.mapNotNull { (tableId, booking) ->
            val key = booking.firstNotNullOfOrNull { if (it.value.id == id) it.key else null }
            if (key != null) tableId to key
            else null
        }.single()

        bookings.computeIfPresent(tableId) { _, bookings ->
            bookings.also { it.remove(key) }
        }
    }

}
