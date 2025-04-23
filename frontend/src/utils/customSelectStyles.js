
const customSelectStyles = {
    control: (base, state) => ({
      ...base,
      backgroundColor: "rgba(0, 0, 0, 0.8)",
      borderColor: "#00f2ff",
      width: "300px",
      boxShadow: state.isFocused ? "0 0 10px #00f2ff" : "0 0 5px #00f2ff",
      color: "#fff",
      "&:hover": {
        borderColor: "#00f2ff"
      }
    }),
    singleValue: (base) => ({
      ...base,
      color: "#00f2ff",
    }),
    option: (base, { isFocused, isSelected }) => ({
      ...base,
      backgroundColor: isFocused
        ? "rgba(0, 242, 255, 0.2)"
        : isSelected
        ? "rgba(0, 242, 255, 0.3)"
        : "transparent",
      color: "#fff",
      cursor: "pointer"
    }),
    menu: (base) => ({
      ...base,
      backgroundColor: "#111",
      border: "1px solid #00f2ff",
      boxShadow: "0 0 10px #00f2ff"
    }),
    input: (base) => ({
      ...base,
      color: "#fff",
    }),
    placeholder: (base) => ({
      ...base,
      color: "#999",
    }),
    clearIndicator: (base) => ({
      ...base,
      color: "#00f2ff",
      "&:hover": { color: "#fff" }
    }),
    dropdownIndicator: (base) => ({
      ...base,
      color: "#00f2ff",
      "&:hover": { color: "#fff" }
    }),
  };
  
  export default customSelectStyles;