const BASE_URL = 'http://localhost:8080';

async function request(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const mergedHeaders = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };
  
  console.log(`[HTTP Request] ${options.method || 'GET'} ${path}`);
  
  const response = await fetch(url, {
    ...options,
    headers: mergedHeaders,
  });

  const text = await response.text();
  let json = null;
  try {
    json = JSON.parse(text);
  } catch (e) {
    json = text;
  }

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}, message: ${JSON.stringify(json)}`);
  }
  return json;
}

async function runTest() {
  console.log('--- Starting Integration Test ---');

  const timestamp = Date.now();
  const roomTypeName = `Deluxe Suite ${timestamp}`;
  const roomNumber = `R-${timestamp.toString().slice(-4)}`;
  const receptionistEmail = `sarah_${timestamp}@gmail.com`;
  const customerEmail = `john_${timestamp}@gmail.com`;

  // 1. Admin login
  console.log('1. Logging in as Admin...');
  const loginResponse = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({
      email: 'admin@gmail.com',
      password: 'admin123'
    })
  });
  const adminToken = loginResponse.token;
  console.log('Admin logged in successfully.');

  // 2. Create Room Type
  console.log(`2. Creating Room Type "${roomTypeName}"...`);
  const roomType = await request('/api/admin/roomtype/save', {
    method: 'POST',
    headers: { Authorization: `Bearer ${adminToken}` },
    body: JSON.stringify({
      name: roomTypeName,
      description: 'A spacious deluxe suite with modern amenities',
      pricePerNight: 150.0,
      capacity: 2,
      status: 'ACTIVE'
    })
  });
  console.log(`Room Type created with ID: ${roomType.roomTypeId}`);

  // 3. Create Room
  console.log(`3. Creating Room "${roomNumber}"...`);
  const room = await request('/api/admin/room/save', {
    method: 'POST',
    headers: { Authorization: `Bearer ${adminToken}` },
    body: JSON.stringify({
      roomNumber: roomNumber,
      roomTypeId: roomType.roomTypeId,
      floorNumber: 2,
      roomStatus: 'AVAILABLE'
    })
  });
  console.log(`Room created with ID: ${room.roomId}`);

  // 4. Create Receptionist
  console.log(`4. Creating Receptionist (${receptionistEmail})...`);
  const receptionist = await request('/api/admin/users/receptionist', {
    method: 'POST',
    headers: { Authorization: `Bearer ${adminToken}` },
    body: JSON.stringify({
      firstName: 'Sarah',
      lastName: 'Connor',
      email: receptionistEmail,
      phone: `8${timestamp.toString().slice(-9)}`,
      gender: 'FEMALE',
      password: 'password123',
      role: 'RECEPTIONIST'
    })
  });
  console.log(`Receptionist created successfully.`);

  // 5. Register Customer
  console.log(`5. Registering Customer (${customerEmail})...`);
  const customer = await request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({
      firstName: 'John',
      lastName: 'Doe',
      email: customerEmail,
      phone: `7${timestamp.toString().slice(-9)}`,
      gender: 'MALE',
      password: 'password123'
    })
  });
  console.log(`Customer registered successfully.`);

  // 6. Customer Login
  console.log('6. Logging in as Customer...');
  const custLogin = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({
      email: customerEmail,
      password: 'password123'
    })
  });
  const customerToken = custLogin.token;
  console.log('Customer logged in successfully.');

  // 7. Search Availability
  console.log('7. Searching availability...');
  const todayStr = new Date().toISOString().split('T')[0];
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const tomorrowStr = tomorrow.toISOString().split('T')[0];

  const searchResults = await request('/api/availability/search', {
    method: 'POST',
    headers: { Authorization: `Bearer ${customerToken}` },
    body: JSON.stringify({
      roomTypeId: roomType.roomTypeId,
      checkInDate: todayStr,
      checkOutDate: tomorrowStr
    })
  });
  console.log('Availability status:', searchResults.available);

  // 8. Create Reservation
  console.log('8. Creating Reservation...');
  const reservation = await request('/api/reservation/save', {
    method: 'POST',
    headers: { Authorization: `Bearer ${customerToken}` },
    body: JSON.stringify({
      userId: customer.userId,
      roomTypeId: roomType.roomTypeId,
      checkInDate: todayStr,
      checkOutDate: tomorrowStr,
      guestCount: 1,
      paymentMethod: 'CARD',
      specialRequest: 'High floor please'
    })
  });
  console.log(`Reservation created successfully in status: ${reservation.reservationStatus}`);

  // 9. Fetch and Process Payment
  console.log('9. Fetching Payment for Reservation...');
  const payment = await request(`/api/payment/reservation/${reservation.reservationId}`, {
    headers: { Authorization: `Bearer ${customerToken}` }
  });
  console.log(`Found Payment ID: ${payment.paymentId} in status: ${payment.paymentStatus}`);

  console.log('10. Starting Payment process...');
  const startPayment = await request(`/api/payment/start/${payment.paymentId}`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${customerToken}` }
  });
  console.log(`Payment status updated to: ${startPayment.paymentStatus}`);

  console.log('11. Completing Payment successfully...');
  const successPayment = await request(`/api/payment/success/${payment.paymentId}?gatewayPaymentId=g_pay_999&gatewaySignature=sig_999`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${customerToken}` }
  });
  console.log(`Payment status after success: ${successPayment.paymentStatus}`);

  // 12. Receptionist Login
  console.log(`12. Logging in as Receptionist (${receptionistEmail})...`);
  const recLogin = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({
      email: receptionistEmail,
      password: 'password123'
    })
  });
  const recToken = recLogin.token;
  console.log('Receptionist logged in successfully.');

  // 13. Assign Room
  console.log(`13. Assigning Room ${roomNumber} to the Reservation...`);
  const roomAssignment = await request('/api/reception/assign-room', {
    method: 'POST',
    headers: { Authorization: `Bearer ${recToken}` },
    body: JSON.stringify({
      reservationId: reservation.reservationId,
      roomId: room.roomId
    })
  });
  console.log(`Room assigned successfully. Assignment ID: ${roomAssignment.assignmentId}`);

  // 13.5 Enter Guest Details
  console.log('13.5 Entering guest details...');
  const guestDetails = await request('/api/guest/save', {
    method: 'POST',
    headers: { Authorization: `Bearer ${recToken}` },
    body: JSON.stringify({
      reservationId: reservation.reservationId,
      firstName: 'Alice',
      lastName: 'Smith',
      phone: '1234567890',
      gender: 'FEMALE',
      dateOfBirth: '1995-05-15'
    })
  });
  console.log('Guest details entered successfully.');

  // 14. Check In
  console.log('14. Checking in guest...');
  const checkInRes = await request('/api/reception/check-in', {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${recToken}` },
    body: JSON.stringify({
      reservationId: reservation.reservationId,
      roomId: room.roomId
    })
  });
  console.log(`Guest checked in successfully.`);

  // 15. Check Out
  console.log('15. Checking out guest...');
  const checkOutRes = await request('/api/reception/check-out', {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${recToken}` },
    body: JSON.stringify({
      reservationId: reservation.reservationId,
      roomId: room.roomId
    })
  });
  console.log(`Guest checked out successfully.`);

  console.log('--- ALL INTEGRATION TESTS PASSED SUCCESSFULLY! ---');
}

runTest().catch(err => {
  console.error('Test Failed:', err);
  process.exit(1);
});
