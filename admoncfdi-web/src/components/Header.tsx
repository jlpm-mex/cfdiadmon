import { useState } from 'react';
import {
  Menu,
  X,
  Home,
  FileText,
  Users,
  Settings,
  User,
  LogOut,
  ChevronDown,
  Receipt
} from 'lucide-react';

const Header = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  // Enlaces de navegación
  const navLinks = [
    { name: 'Inicio', href: '/', icon: Home },
    { name: 'CFDIs', href: '/cfdis', icon: Receipt },
  ];

  return (
    <nav className="bg-linear-to-r from-blue-600 to-blue-800 border-b border-blue-700 shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">

          {/* Logo y Título - Lado izquierdo */}
          <div className="flex items-center space-x-3">
            <div className="shrink-0 bg-white/10 p-2 rounded-lg backdrop-blur-sm text-white">
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"
                />
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M8 13h8M8 17h5"
                  strokeOpacity={0.7}
                />
              </svg>
            </div>

            {/* TÍTULO PRINCIPAL */}
            <div className="flex flex-col">
              <h1 className="text-xl font-bold text-white tracking-wide leading-none">
                Administrador de CFDI
              </h1>
              <span className="text-xs text-blue-200 font-medium tracking-wider uppercase">
                Tus CFDI's en un solo lugar
              </span>
            </div>
          </div>

          {/* Navegación Desktop - Lado central */}
          <div className="hidden md:flex items-center space-x-1">
            {navLinks.map((link) => {
              const Icon = link.icon;
              return (
                <a
                  key={link.name}
                  href={link.href}
                  className="flex items-center px-3 py-2 rounded-lg text-sm font-medium text-blue-100 hover:text-white hover:bg-white/10 transition-all duration-200 group"
                >
                  <Icon className="w-4 h-4 mr-2 group-hover:scale-110 transition-transform" />
                  {link.name}
                </a>
              );
            })}
          </div>

          {/* Menú de Usuario - Lado derecho */}
          {/* <div className="hidden md:flex items-center space-x-4">
            <div className="relative">
              <button
                onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                className="flex items-center space-x-2 text-white hover:bg-white/10 rounded-lg px-3 py-2 transition-all duration-200"
              >
                <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">
                  <User className="w-4 h-4" />
                </div>
                <span className="text-sm font-medium">Admin</span>
                <ChevronDown className={`w-4 h-4 transition-transform duration-200 ${isUserMenuOpen ? 'rotate-180' : ''}`} />
              </button> */}

          {/* Dropdown usuario */}
          {/* {isUserMenuOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-xl py-1 border border-gray-100 z-50">
                  <a href="/perfil" className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-blue-50 transition-colors">
                    <User className="w-4 h-4 mr-3" />
                    Mi Perfil
                  </a>
                  <a href="/configuracion" className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-blue-50 transition-colors">
                    <Settings className="w-4 h-4 mr-3" />
                    Configuración
                  </a>
                  <hr className="my-1" />
                  <button className="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors">
                    <LogOut className="w-4 h-4 mr-3" />
                    Cerrar Sesión
                  </button>
                </div>
              )}
            </div>
          </div> */}

          {/* Botón menú móvil */}
          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="md:hidden text-white hover:bg-white/10 p-2 rounded-lg transition-colors"
          >
            {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Menú Móvil */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-t border-blue-700/50 bg-blue-700/30 backdrop-blur-sm">
          <div className="px-2 pt-2 pb-3 space-y-1">
            {navLinks.map((link) => {
              const Icon = link.icon;
              return (
                <a
                  key={link.name}
                  href={link.href}
                  className="flex items-center px-3 py-2 rounded-lg text-base font-medium text-blue-100 hover:text-white hover:bg-white/10 transition-colors"
                >
                  <Icon className="w-5 h-5 mr-3" />
                  {link.name}
                </a>
              );
            })}
            <hr className="border-blue-700/50 my-2" />
            <button className="flex items-center w-full px-3 py-2 rounded-lg text-base font-medium text-red-300 hover:text-red-200 hover:bg-white/10 transition-colors">
              <LogOut className="w-5 h-5 mr-3" />
              Cerrar Sesión
            </button>
          </div>
        </div>
      )}
    </nav>
  );
}

export default Header