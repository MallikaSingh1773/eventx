import { useState, useCallback } from 'react';
import { toast } from 'react-toastify';

export const useApi = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const execute = useCallback(async (apiCall, options = {}) => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiCall();
      if (options.successMessage) {
        toast.success(options.successMessage);
      }
      return response;
    } catch (err) {
      const message = err.response?.data?.message || err.message || 'Something went wrong';
      setError(message);
      if (options.showError !== false) {
        toast.error(message);
      }
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return { loading, error, execute };
};
