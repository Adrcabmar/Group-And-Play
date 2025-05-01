const customMultiSelectStyles = {
    control: (base, state) => ({
      ...base,
      backgroundColor: "rgba(31, 26, 51, 0.95)",
      borderColor: "#00f2ff",
      boxShadow: state.isFocused ? "0 0 10px #00f2ff" : "none",
      color: "#00f2ff",
      "&:hover": {
        borderColor: "#00f2ff"
      }
    }),
    menu: (base) => ({
      ...base,
      backgroundColor: "rgba(31, 26, 51, 0.95)",
      border: "1px solid #00f2ff",
      boxShadow: "0 0 10px #00f2ff",
      zIndex: 100,
    }),
    option: (base, { isFocused, isSelected }) => ({
      ...base,
      backgroundColor: isSelected
        ? "rgba(0, 242, 255, 0.3)"
        : isFocused
        ? "rgba(0, 242, 255, 0.15)"
        : "transparent",
      color: "#00f2ff",
      cursor: "pointer",
    }),
    multiValue: (base) => ({
      ...base,
      backgroundColor: "#00f2ff22",
      color: "#00f2ff",
    }),
    multiValueLabel: (base) => ({
      ...base,
      color: "#00f2ff",
    }),
    multiValueRemove: (base) => ({
      ...base,
      color: "#00f2ff",
      ":hover": {
        backgroundColor: "#00f2ff",
        color: "black",
      },
    }),
    singleValue: (base) => ({
      ...base,
      color: "#00f2ff",
    }),
    input: (base) => ({
      ...base,
      color: "#00f2ff",
    }),
    placeholder: (base) => ({
      ...base,
      color: "#88e5ff",
    }),
    dropdownIndicator: (base) => ({
      ...base,
      color: "#00f2ff",
    }),
    clearIndicator: (base) => ({
      ...base,
      color: "#00f2ff",
    }),
  };

  export default customMultiSelectStyles;