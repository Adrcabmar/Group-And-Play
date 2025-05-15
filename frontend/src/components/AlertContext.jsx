/* eslint-disable react/prop-types */
import { createContext, useState, useContext } from "react";
import Alert from "./Alert";

const AlertContext = createContext();

export function AlertProvider({ children }) {
  const [notification, setAlert] = useState({ message: "", type: "" });

  const showAlert = (message, type = "info") => {
    setAlert({ message, type });
    setTimeout(() => setAlert({ message: "", type: "" }), 4000);
  };

  return (
    <AlertContext.Provider value={{ showAlert }}>
      {children}
      <Alert
        message={notification.message}
        type={notification.type}
        onClose={() => setAlert({ message: "", type: "" })}
      />
    </AlertContext.Provider>
  );
}

export const useAlert = () => useContext(AlertContext);