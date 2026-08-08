import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import { authApiSlice } from '../features/auth/authApiSlice';
import { movieApiSlice } from '../features/movies/movieApiSlice';
import { theaterApiSlice } from '../features/theaters/theaterApiSlice';
import { showApiSlice } from '../features/shows/showApiSlice';
import { bookingApiSlice } from '../features/bookings/bookingApiSlice';
import { paymentApiSlice } from '../features/payments/paymentApiSlice';
import { walletApiSlice } from '../features/wallet/walletApiSlice';
import { notificationApiSlice } from '../features/notifications/notificationApiSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [authApiSlice.reducerPath]: authApiSlice.reducer,
    [movieApiSlice.reducerPath]: movieApiSlice.reducer,
    [theaterApiSlice.reducerPath]: theaterApiSlice.reducer,
    [showApiSlice.reducerPath]: showApiSlice.reducer,
    [bookingApiSlice.reducerPath]: bookingApiSlice.reducer,
    [paymentApiSlice.reducerPath]: paymentApiSlice.reducer,
    [walletApiSlice.reducerPath]: walletApiSlice.reducer,
    [notificationApiSlice.reducerPath]: notificationApiSlice.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(authApiSlice.middleware)
      .concat(movieApiSlice.middleware)
      .concat(theaterApiSlice.middleware)
      .concat(showApiSlice.middleware)
      .concat(bookingApiSlice.middleware)
      .concat(paymentApiSlice.middleware)
      .concat(walletApiSlice.middleware)
      .concat(notificationApiSlice.middleware),
});
