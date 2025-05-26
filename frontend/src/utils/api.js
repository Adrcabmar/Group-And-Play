import axios from "axios";

const apiUrl = import.meta.env.VITE_API_URL;

export const loginUser = async (form) => {
  const response = await axios.post(`${apiUrl}/api/auth/login`, form);
  return response.data;
};

export const registerUser = async (form) => {
  await axios.post(`${apiUrl}/api/users`, form);
};

export const getCurrentUser = async ({token}) => {
  const response = await fetch(`${apiUrl}/api/users/auth/current-user`, {
    method: "GET",
    headers: {
    "Authorization": `Bearer ${token}`,  
    "Content-Type": "application/json",
    },
    credentials: "include",
  });

    const data = await response.json();

    return data;
}