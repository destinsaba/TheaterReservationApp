import React, { useState, useEffect } from 'react';
import { useShowtime } from '../context/ShowtimeContext'; // Import custom context for selected options
import useApi from '../hooks/useApi'; // Ensure this is your custom hook
import { useRouter } from 'next/navigation';

const PaymentWindow = () => {
  const { selectedMovie, selectedTheater, selectedShowtime, selectedSeats } = useShowtime();
  const router = useRouter();

  // Log the values from ShowtimeContext
  useEffect(() => {
    console.log('Showtime Context:', {
      selectedMovie,
      selectedTheater,
      selectedShowtime,
      selectedSeats
    });
  }, [selectedMovie, selectedTheater, selectedShowtime, selectedSeats]);

  const [cardNumber, setCardNumber] = useState('');
  const [expiryDate, setExpiry] = useState('');
  const [cvc, setCvc] = useState('');
  const [email, setEmail] = useState('');
  const [coupon, setCoupon] = useState('');
  const [discount, setDiscount] = useState(0);
  const [totalAmount, setTotalAmount] = useState(0); // Example base price
  const [finalAmount, setFinalAmount] = useState(totalAmount);
  
  // Error messages state
  const [errors, setErrors] = useState({
    cardNumber: '',
    expiryDate: '',
    cvc: '',
    email: ''
  });

  const userID = parseInt(localStorage.getItem('userID'), 10) || null;


  const { callApi: getSavedInfo, data: userInfo, loading, error } = useApi(
    userID ? `http://localhost:8080/api/users/${userID}` : null,
    'GET'
  );

  const { callApi: makePayment, data: payData, loading: payLoading, error: payError } = useApi(
    'http://localhost:8080/api/payment/tickets', 'POST'
  );

  // Use the API for getting a coupon
  const { callApi: applyCoupon, data: appliedCoupon, error: couponApplicationError } = useApi(
    coupon ? `http://localhost:8080/api/coupons/${coupon}` : null,
    'GET'
  );

  // Use the API to redeem a coupon
  const { callApi: redeemCoupon, data: redeemedCoupon, error: couponRedemptionError } = useApi(
    coupon ? `http://localhost:8080/api/coupons/redeem/${coupon}` : null,
    'PUT'
  );

  // Use API to get cost of tickets
  const { callApi: getTicketCost, data: ticketCost, error: ticketCostError } = useApi(
    'http://localhost:8080/api/tickets/cost', 'POST'
  );

  // Autofill form with saved user data
  const handleUseSavedPaymentInfo = async () => {
    await getSavedInfo(); // Fetch user data
    console.log('User data:', userInfo);
  };

  useEffect(() => {
    if (userInfo) {
      console.log('User data:', userInfo);
      setCardNumber(userInfo.cardNumber || '');
      setExpiry(userInfo.expiryDate || '');
      setCvc(userInfo.cvc || '');
      setEmail(userInfo.email || '');
    }
  }, [userInfo]); 


  // Handle coupon application
  const useCoupon = async () => {
    try {
    if (coupon) {
      await applyCoupon();
    }
   } catch (err) {
      const errorMessage = err.response?.data || 'Payment failed, please try again.';
      alert(errorMessage);
    }
  };

  useEffect(() => {
    if (appliedCoupon) {
      setDiscount(appliedCoupon.amount);
      setFinalAmount(totalAmount - appliedCoupon.amount);
    }
  }, [appliedCoupon]);

  // Calculate total amount
  useEffect(() => {
    if (selectedSeats.length > 0) {
      getTicketCost(selectedSeats); // Fetch ticket cost
    }
  }, [selectedSeats]);
  
  // Ensure that ticketCost is set after API response is received
  useEffect(() => {
    if (ticketCost) {
      setTotalAmount(ticketCost); // Set the base ticket cost
      setFinalAmount(ticketCost - discount); // Apply discount to the total amount
    }
  }, [ticketCost, discount]);

  const validateField = (name, value) => {
    let errorMessage = '';
  
    switch (name) {
      case 'email':
        errorMessage = /\S+@\S+\.\S+/.test(value) ? '' : 'Invalid email format.';
        break;
      case 'cardNumber':
        errorMessage = /^\d{16}$/.test(value) ? '' : 'Card number must be 16 digits.';
        break;
      case 'expiryDate':
        errorMessage = /^\d{4}$/.test(value) ? '' : 'Expiry date must be in MMYY format.';
        break;
      case 'cvc':
        errorMessage = /^\d{3,4}$/.test(value) ? '' : 'CVC must be 3 or 4 digits.';
        break;
      default:
        break;
    }

    return errorMessage;
  };

  // Handle input changes
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    const errorMessage = validateField(name, value);

    // Update state
    switch (name) {
      case 'cardNumber':
        setCardNumber(value);
        break;
      case 'expiryDate':
        setExpiry(value);
        break;
      case 'cvc':
        setCvc(value);
        break;
      case 'email':
        setEmail(value);
        break;
      default:
        break;
    }

    setErrors((prevErrors) => ({
      ...prevErrors,
      [name]: errorMessage
    }));
  };

  const handlePayment = async (e) => {
    console.log('handlePayment called');
    e.preventDefault();

    // Ensure there are no validation errors
    if (Object.values(errors).some((error) => error)) {
      alert('Please correct the errors before submitting.');
      return;
    }

    const paymentDetails = {
      showtimeId: selectedShowtime.id,
      seatIds: selectedSeats,
      cardNumber,
      email,
      price: finalAmount,
    };

    if (userID) {
      paymentDetails.registeredUserId = userID;
    }

    try {
      console.log('Payment details:', paymentDetails);
      const response = await makePayment(paymentDetails);

      if (response) {
        if (appliedCoupon) {
          await redeemCoupon(); // Redeem the coupon after payment
        }
        alert('Payment successful!');
        // Redirect to confirmation or home page
        router.push('/');
      }
    } catch (err) {
      const errorMessage = err.response?.data || 'Payment failed, please try again.';
      alert(errorMessage);
    }
  };

  return (
    <div className="p-6 max-w-md mx-auto bg-white rounded shadow-md mt-16">
      <h2 className="text-2xl font-bold mb-4 text-black">Confirm Payment Details</h2>

      {userID && !loading && (
        <button
          type="button"
          onClick={handleUseSavedPaymentInfo}
          className="w-full mb-4 bg-[#854d0e] hover:bg-[#a16207] text-white py-2 rounded-lg font-bold"
        >
          Use Saved Payment Information
        </button>
      )}

      {loading && <p className="text-gray-700 mb-4">Loading saved payment information...</p>}
      {error && <p>Error loading saved payment information. Please try again.</p>}

      <form onSubmit={handlePayment}>
        {/* Card Information */}
        <div className="mb-4">
          <label className="block text-gray-700">Card Number</label>
          <input
            type="text"
            name="cardNumber"
            value={cardNumber}
            onChange={handleInputChange}
            className="w-full border border-gray-300 rounded-lg px-4 py-2 text-black"
            placeholder="Card Number"
            required
          />
          {errors.cardNumber && <p className="text-red-500 text-sm">{errors.cardNumber}</p>}
        </div>
        <div className="mb-4">
          <label className="block text-gray-700">Expiry Date</label>
          <input
            type="text"
            name="expiryDate"
            value={expiryDate}
            onChange={handleInputChange}
            className="w-full border border-gray-300 rounded-lg px-4 py-2 text-black"
            placeholder="MMYY"
            required
          />
          {errors.expiryDate && <p className="text-red-500 text-sm">{errors.expiryDate}</p>}
        </div>
        <div className="mb-4">
          <label className="block text-gray-700">CVC</label>
          <input
            type="text"
            name="cvc"
            value={cvc}
            onChange={handleInputChange}
            className="w-full border border-gray-300 rounded-lg px-4 py-2 text-black"
            placeholder="CVC"
            required
          />
          {errors.cvc && <p className="text-red-500 text-sm">{errors.cvc}</p>}
        </div>

        {/* Email Field */}
        <div className="mb-4">
          <label className="block text-gray-700">Email</label>
          <input
            type="email"
            name="email"
            value={email}
            onChange={handleInputChange}
            className="w-full border border-gray-300 rounded-lg px-4 py-2 text-black"
            placeholder="Email"
            required
          />
          {errors.email && <p className="text-red-500 text-sm">{errors.email}</p>}
        </div>

        {/* Coupon Code */}
        <div className="mb-4">
          <label className="block text-gray-700">Coupon Code</label>
          <div className="flex">
            <input
              type="text"
              value={coupon}
              onChange={(e) => setCoupon(e.target.value)}
              className="flex-grow border border-gray-300 rounded-l-lg px-4 py-2 text-black"
              placeholder="Enter coupon code"
            />
            <button
              type="button"
              onClick={useCoupon}
              className="bg-[#854d0e] hover:bg-[#a16207] text-white px-4 py-2 rounded-r-lg"
            >
              Apply
            </button>
          </div>
        </div>

        {/* Display Amount and Discount */}
        <div className="mb-6 text-lg font-semibold text-black">
          <p>Subtotal: ${totalAmount}</p>
          {discount > 0 && <p>Discount: -${discount.toFixed(2)}</p>}
          <p className="text-xl">Final Amount: ${finalAmount.toFixed(2)}</p>
        </div>

        <button
          type="submit"
          onClick={(e) => {console.log('Button clicked');
            handlePayment;
          }}
          className="w-full bg-[#854d0e] hover:bg-[#a16207] text-white py-2 rounded-lg font-bold"
        >
          {payLoading ? 'Processing Payment...' : 'Complete Payment'}
        </button>
      </form>
    </div>
  );
};

export default PaymentWindow;
