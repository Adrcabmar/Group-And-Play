/* eslint-disable react/prop-types */
import "../static/resources/css/Alert.css";

function Alert({ message, type, onClose }) {
  if (!message) return null;

  return (
    <div className={`alert ${type || "info"}`}>
      <span>{message}</span>
      <button className="alert-close" onClick={onClose}>×</button>
    </div>
  );
}

export default Alert;