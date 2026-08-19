import { useState, useRef, useEffect, useMemo } from 'react';

export interface DropdownOption {
    label: string;
    value: string | number;
}

export type RawOption = DropdownOption | string | number;

interface DropdownProps {
    label: string;
    data: RawOption[];
    value?: string | number;
    onSelect?: (value: string | number) => void;
    placeholder?: string;
}

const normalizeOption = (item: RawOption): DropdownOption => {
    if (typeof item === 'object' && item !== null && 'label' in item && 'value' in item) {
        return item;
    }
    return { label: String(item), value: item };
};

const Dropdown = ({ label, data, value, onSelect, placeholder = "Seleccionar" }: DropdownProps) => {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const [searchTerm, setSearchTerm] = useState<string>("");

    const toggleDropdown = () => {
        setIsOpen((prev) => !prev);
    };

    const options = data.map(normalizeOption);
    const selectedOption = options.find((opt) => String(opt.value) === String(value));

    const handleSelectOption = (optionValue: string | number) => {
        onSelect?.(optionValue);
        setIsOpen(false);
    };

    const filteredOptions = useMemo<DropdownOption[]>(() => {
        if (!searchTerm.trim()) return options;
        const term = searchTerm.toLowerCase().trim();
        return options.filter(opt => opt.label.toLowerCase().includes(term));
    },
    [options, searchTerm]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Escape') {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
            document.addEventListener('keydown', handleKeyDown);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, [isOpen]);

    return (
        <>
            <label className="font-mono text-[11px] uppercase tracking-widest text-slate-400">{label}</label>

            <div ref={dropdownRef} className="relative w-full">
                <button
                    onClick={toggleDropdown}
                    type="button"
                    className="flex w-full items-center 
                    justify-between 
                    rounded-lg 
                    border 
                    border-white/10 
                    bg-white/[0.04] 
                    px-3 
                    py-2.5 
                    text-sm 
                    text-slate-100 outline-none 
                    focus:border-teal-400"
                >
                    <span className={`
                        ${selectedOption ? "text-slate-100 font-medium" : "text-slate-400"}
                        truncate
                        flex-1
                        min-w-0
                        max-w-[235.59px]
                        text-left
                    `}>
                        {selectedOption ? selectedOption.label : placeholder}
                    </span>
                    <svg
                        className={`h-5 w-5 text-slate-400 transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`}
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        aria-hidden="true"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M19 9l-7 7-7-7"
                        />
                    </svg>
                </button>

                {isOpen && (
                    <div
                        className="absolute 
                        right-0 
                        z-10 
                        mt-2 
                        max-h-60 
                        overflow-y-auto 
                        w-full 
                        origin-top-right 
                        rounded-md 
                        bg-[#16263D] 
                        border 
                        border-white/10 
                        shadow-lg 
                        ring-1 
                        ring-black 
                        ring-opacity-5 
                        focus:outline-none 
                        scrollbar-thin"
                        role="menu"
                    >
                        <div className="py-1">
                            <input className="block 
                            w-[90%] 
                            text-left 
                            rounded-lg border 
                            border-white/10 
                            bg-white/[0.04] 
                            px-3 
                            py-2.5 
                            text-sm 
                            text-slate-100 
                            outline-none 
                            focus:border-teal-400 
                            m-auto" 
                            placeholder='Filtrar'
                            onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            {options.length === 0 ? (
                                <div className="px-4 py-2 text-sm text-slate-400 italic">No hay opciones</div>
                            ) : (
                                filteredOptions.map((opt, i) => (
                                    <button
                                        key={`${opt.value}-${i}`}
                                        type="button"
                                        onClick={() => handleSelectOption(opt.value)}
                                        className={`block w-full text-left px-4 py-2 text-sm transition-colors ${
                                            String(opt.value) === String(value)
                                                ? "bg-teal-400/20 text-teal-300 font-semibold"
                                                : "text-slate-200 hover:bg-white/[0.08]"
                                        }`}
                                        role="menuitem"
                                    >
                                        {opt.label}
                                    </button>
                                ))
                            )}
                        </div>
                    </div>
                )}
            </div>
        </>
    );
};

export default Dropdown;