class WaterMark {
    //声明私有字段
    #element;
    #text;
    #settings;

    //声明私有字段
    constructor(element, text, settings = {}) {
        this.#element = element || document.documentElement;
        this.#text = text;
        this.#settings = {
            "offsetX": settings.offsetX || 20,                     // 水印起始位置x轴坐标
            "offsetY": settings.offsetY || 20,                     // 水印起始位置y轴坐标
            "spacingX": settings.spacingX || 150,                // 水印x轴间隔
            "spacingY": settings.spacingY || 100,                // 水印y轴间隔
            "fontSize": settings.fontSize || '22px',    //字体大小
            "fontFamily": settings.fontFamily || window.getComputedStyle(this.#element).fontFamily,
            "opacity": settings.opacity || 0.3,        // 水印透明度
            "rotateAngle": settings.rotateAngle || 20,           // 水印倾斜度
            "fontColor": settings.fontColor || 'grey'
        };
    }

    //创建水印
    create(remarkTime = false) {
        window.onload = () => {
            this.#buildWaterMark(remarkTime);
        };
        window.onresize = () => {
            this.#buildWaterMark(remarkTime);
        };
    }

    // 私有方法：生成水印
    #buildWaterMark(remarkTime = false) {
        // 清除已有的水印
        let watermarks = this.#element.getElementsByClassName("watermark");
        for (let watermark of watermarks) {
            watermark.remove();
        }

        // 获取当前页面的宽度和高度
        let page_width = this.#element.scrollWidth - this.#settings.offsetX;
        let page_height = this.#element.scrollHeight - this.#settings.offsetY;

        let fontSize = Number.parseInt(this.#settings.fontSize.replace('px', ''));
        let width = fontSize * this.#text.length;
        let height = fontSize;

        // 计算水印的列数和行数
        let cols = parseInt(page_width / (this.#settings.spacingX + width)) + 1;
        let rows = parseInt(page_height / (this.#settings.spacingY + height)) + 1;

        let x;
        let y;

        // 循环生成水印
        for (let i = 0; i < rows; i++) {
            for (let j = 0; j < cols; j++) {
                x = this.#settings.offsetX + (this.#settings.spacingX + width) * j;
                y = this.#settings.offsetY + (this.#settings.spacingY + height) * i;
                let new_div = document.createElement("div");
                new_div.className = 'watermark';
                new_div.innerHTML = this.#text + (remarkTime ? `<br>${this.#getNowFormatDay()}` : '');
                new_div.style.transform = "rotate(-" + this.#settings.rotateAngle.toString() + "deg)"; // 旋转
                new_div.style.position = "absolute";
                new_div.style.left = x + 'px';
                new_div.style.top = y + 'px';
                new_div.style.overflow = "hidden";
                new_div.style.zIndex = "9999";
                new_div.style.pointerEvents = "none";
                new_div.style.opacity = this.#settings.opacity;
                new_div.style.fontSize = this.#settings.fontSize;
                new_div.style.fontFamily = this.#settings.fontFamily
                new_div.style.textAlign = "center";
                new_div.style.color = this.#settings.fontColor;
                this.#element.appendChild(new_div);
            }
        }
    }

    // 获取当前时间
    #getNowFormatDay(nowDate = null) {
        let char = "-";
        if (nowDate == null) {
            nowDate = new Date();
        }
        let day = nowDate.getDate();
        let month = nowDate.getMonth() + 1; // 月份从0开始，所以要加1
        let year = nowDate.getFullYear();
        return year + char + this.#completeDate(month) + char + this.#completeDate(day);
    }

    // 补全日期，确保是两位数
    #completeDate(value) {
        return value < 10 ? "0" + value : value;
    }

    setElement(element) {
        this.#element = element;
    }

    // 设置水印文本
    setText(newText) {
        this.#text = newText;
    }

    // 设置水印的透明度
    setOpacity(opacity) {
        this.#settings.opacity = opacity;
    }

    // 设置水印的倾斜角度
    setRotateAngle(rotateAngle) {
        this.#settings.rotateAngle = rotateAngle;
    }

    // 设置水印字体大小
    updateFontSize(fontSize) {
        this.#settings.fontSize = fontSize;
    }


    // 设置水印字体颜色
    setFontColor(fontColor) {
        this.#settings.fontColor = fontColor;
    }

    // 设置水印的字体
    setFontFamily(fontFamily) {
        this.#settings.fontFamily = fontFamily;
    }


    // 设置开始添加水印x轴位置
    setOffsetX(offsetX) {
        this.#settings.offsetX = offsetX;
    }

    // 设置开始添加水印Y轴位置
    setOffsetY(offsetY) {
        this.#settings.offsetY = offsetY;
    }

    // 设置水印间水平间隔大小
    setSpacingX(spacingX) {
        this.#settings.spacingX = spacingX;
    }

    // 设置水印间垂直间隔大小
    setSpacingY(spacingY) {
        this.#settings.spacingY = spacingY;
    }
}
