interface InputFieldProps {
  label: string;
  id: string;
  type: string;
  placeholder: string;
}

const InputField = ({ label, id, type, placeholder }: InputFieldProps) => {
  return (
    <div className='mb-4'>
      <label htmlFor={id} className='text-md mb-2 font-medium text-gray-950'>
        {label}
      </label>
      <input
        type={type}
        id={id}
        placeholder={placeholder}
        className='text-md placeholder:text-md w-full rounded-sm border px-4 py-2 font-medium text-gray-950 placeholder:font-medium placeholder:text-gray-400 focus:ring-2 focus:ring-blue-400 focus:outline-none'
      />
    </div>
  );
};

export default InputField;
