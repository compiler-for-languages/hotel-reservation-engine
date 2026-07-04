import { useMemo } from "react";
import { Calendar, dateFnsLocalizer, Views } from "react-big-calendar";
import { format, parse, startOfWeek, getDay } from "date-fns";
import { enUS } from "date-fns/locale";
import type { ReservationResponseDTO } from "@/types/api";

const locales = { "en-US": enUS };

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek,
  getDay,
  locales,
});

interface OccupancyCalendarProps {
  reservations: ReservationResponseDTO[];
}

export const OccupancyCalendar = ({ reservations }: OccupancyCalendarProps) => {
  const events = useMemo(() => {
    const filtered = reservations.filter((res) => res.reservationStatus === "CONFIRMED" || res.reservationStatus === "CHECKED_IN");
    
    const mapped = filtered.map((res) => {
      // Hotel booking logic: checkout date is exclusive
      // Check-In = July 3, Check-Out = July 5 means occupied on July 3 and 4, not July 5
      const startDate = new Date(res.checkInDate);
      const endDate = new Date(res.checkOutDate);
      
      const event = {
        id: res.reservationId,
        title: res.roomTypeName || "Room",
        start: startDate,
        end: endDate,
        resource: res,
      };
      
      return event;
    });
    
    return mapped;
  }, [reservations]);

  const eventStyleGetter = (event: any) => {
    const status = event.resource?.reservationStatus;
    let backgroundColor = "#667eea";
    if (status === "CHECKED_IN") {
      backgroundColor = "#10b981";
    }
    return {
      style: {
        backgroundColor,
        borderRadius: "4px",
        border: "none",
        color: "white",
        fontSize: "10px",
        padding: "2px 4px",
        height: "20px",
        lineHeight: "16px",
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
      },
    };
  };

  const handleSelectEvent = (event: any) => {
    const reservation = event.resource as ReservationResponseDTO;
    alert(`Reservation Details:\n\nID: ${reservation.reservationId}\nCustomer: ${reservation.userName}\nRoom Type: ${reservation.roomTypeName}\nStatus: ${reservation.reservationStatus}\nCheck-In: ${reservation.checkInDate}\nCheck-Out: ${reservation.checkOutDate}\nGuest Count: ${reservation.guestCount}`);
  };

  return (
    <div className="card-elegant p-5">
      <h3 className="text-lg font-semibold text-slate-900 mb-4">Room Occupancy Calendar</h3>
      <div style={{ height: "500px" }}>
        <Calendar
          localizer={localizer}
          events={events}
          startAccessor="start"
          endAccessor="end"
          style={{ height: "100%" }}
          views={[Views.MONTH, Views.WEEK, Views.DAY]}
          defaultView={Views.MONTH}
          eventPropGetter={eventStyleGetter}
          onSelectEvent={handleSelectEvent}
          className="occupancy-calendar"
          key={reservations.length} // Force re-render when reservations change
        />
      </div>
    </div>
  );
};
